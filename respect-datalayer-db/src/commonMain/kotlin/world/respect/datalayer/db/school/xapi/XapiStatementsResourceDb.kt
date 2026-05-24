package world.respect.datalayer.db.school.xapi

import androidx.room.Transactor
import androidx.room.useReaderConnection
import androidx.room.useWriterConnection
import io.ktor.client.utils.buildHeaders
import io.ktor.http.HttpHeaders
import io.ktor.http.Url
import io.ktor.util.date.GMTDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.ext.toLongPair
import world.respect.datalayer.db.school.xapi.adapters.ActorEntities
import world.respect.datalayer.db.school.xapi.adapters.StatementEntities
import world.respect.datalayer.db.school.xapi.adapters.VerbEntities
import world.respect.datalayer.db.school.xapi.adapters.identifierHash
import world.respect.datalayer.db.school.xapi.adapters.toEntities
import world.respect.datalayer.db.school.xapi.adapters.toModel
import world.respect.datalayer.db.school.xapi.adapters.toVerbEntities
import world.respect.datalayer.db.school.xapi.adapters.toXapiAssignmentResult
import world.respect.datalayer.db.school.xapi.composites.XapiStatementAndJsonEntities
import world.respect.datalayer.db.school.xapi.daos.XapiStatementEntityDao
import world.respect.datalayer.school.xapi.XapiActivityDataSourceLocal
import world.respect.lib.xapi.resources.XapiStatementsResource
import world.respect.datalayer.school.xapi.XapiStatementsResourceLocal
import world.respect.datalayer.school.xapi.ext.allDefinedActivities
import world.respect.lib.xapi.model.XapiStatement
import kotlin.time.Clock
import kotlin.uuid.Uuid
import world.respect.datalayer.db.school.xapi.entities.XapiStatementEntityObjectTypeEnum
import world.respect.datalayer.db.school.xapi.ext.allActivityUids
import world.respect.datalayer.db.school.xapi.ext.allActorUids
import world.respect.datalayer.ext.appendIfNotNull
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.datalayer.school.xapi.XapiActorDataSourceLocal
import world.respect.datalayer.school.xapi.ext.allActors
import world.respect.datalayer.school.xapi.ext.allDefinedVerbs
import world.respect.datalayer.school.xapi.ext.distinctMerged
import world.respect.datalayer.school.xapi.ext.copyWithIdIfNotSet
import world.respect.lib.dataloadstate.DataLoadMetaInfo
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.DataReadyState
import world.respect.lib.dataloadstate.NoDataLoadedState
import world.respect.lib.xapi.OpenEelXapiConstants.HEADER_XAPI_CONSISTENT_THROUGH
import world.respect.lib.xapi.OpenEelXapiConstants.HEADER_XAPI_VERSION
import world.respect.lib.xapi.composites.AssignmentAndProgress
import world.respect.lib.xapi.composites.XapiActorAndAssignmentProgress
import world.respect.lib.xapi.composites.XapiAssignmentProgress
import world.respect.lib.xapi.exceptions.XapiBadRequestException
import world.respect.lib.xapi.exceptions.XapiForbiddenException
import world.respect.lib.xapi.ext.lastModifiedGMTStringForRetrievedStatements
import world.respect.lib.xapi.ext.mostRecentByTimestampOrNull
import world.respect.lib.xapi.model.XapiActor
import world.respect.lib.xapi.model.XapiAgent
import world.respect.lib.xapi.model.XapiGroup
import world.respect.lib.xapi.model.XapiStatementRef
import world.respect.lib.xapi.model.XapiStatementResult
import world.respect.lib.xapi.model.XapiStatementTransformingSerializer
import world.respect.lib.xapi.model.XapiVerb
import world.respect.lib.xapi.resources.XapiStatementsResource.GetStatementParams
import kotlin.time.Instant

class XapiStatementsResourceDb(
    private val schoolDb: RespectSchoolDatabase,
    private val authenticatedUser: AuthenticatedUserPrincipalId,
    private val schoolUrl: Url,
    private val uidNumberMapper: UidNumberMapper,
    private val json: Json,
    private val xapiActivityDataSourceLocal: XapiActivityDataSourceLocal,
    private val xapiActorDataSourceLocal: XapiActorDataSourceLocal,
    private val xapiVersion: String = XAPI_VERSION,
) : XapiStatementsResourceLocal {

    suspend fun doUpsertStatement(
        stmt: XapiStatement
    ) {
        //The statement received via HTTP should NOT have a stored time. If it does, we MUST remove
        //it to ensure concurrency and consistency results work as expected. This is allowed here
        //only to enable testing of exact statement storage.
        val storedTime = stmt.stored ?: Clock.System.now()

        val stmtTimestamp = stmt.timestamp ?: storedTime

        val exactStatement = stmt.copy(
            stored = storedTime,
            timestamp = stmtTimestamp,
            id = stmt.id ?: Uuid.random(),
        )

        val statementEntity = exactStatement.toEntities(
            uidNumberMapper = uidNumberMapper,
            json = json,
            isSubStatement = false,
        )

        schoolDb.getStatementDao().insertOrIgnoreListAsync(statementEntity.statements)
        schoolDb.getStatementEntityJsonDao().insertOrIgnoreListAsync(
            statementEntity.statementEntityJson
        )
        schoolDb.getStatementContextActivityJoinDao().insertOrIgnoreListAsync(
            statementEntity.statementContextActivityJoins
        )

        val verbEntities = stmt.allDefinedVerbs().map {
            it.toVerbEntities(uidNumberMapper)
        }

        schoolDb.getVerbDao().insertOrIgnoreAsync(
            verbEntities.map { it.verbEntity }
        )

        schoolDb.getVerbLangMapEntryDao().upsertList(
            verbEntities.flatMap { it.verbLangMapEntries }
        )

        val activities = stmt.allDefinedActivities().distinctMerged()
        xapiActivityDataSourceLocal.updateLocal(activities, stmtTimestamp)
        xapiActorDataSourceLocal.updateLocal(
            actors = stmt.allActors().distinctMerged(),
            timestamp = stmtTimestamp,
        )
    }

    private suspend fun List<XapiStatementAndJsonEntities>.mapToCanonicalStatements(
        idOnly: Boolean
    ): List<XapiStatement> {
        return map { entity ->
            val substatementEntity = schoolDb.takeIf {
                entity.stmtEntity.statementObjectType == XapiStatementEntityObjectTypeEnum.SUBSTATEMENT
            }?.getStatementDao()?.getEntityForSubstatement(
                subStatementIdHi = entity.stmtEntity.statementObjectUid1,
                subStatementIdLo = entity.stmtEntity.statementObjectUid2,
            )

            //now get StatementContextActivityJoins
            val contextActivityJoins = schoolDb.getStatementContextActivityJoinDao()
                .findAllByStatementIds(
                    statementIdHi = entity.stmtEntity.statementIdHi,
                    statementIdLo = entity.stmtEntity.statementIdLo,
                    statementIdHi2 = substatementEntity?.stmtEntity?.statementIdHi ?: 0,
                    statementIdLo2 = substatementEntity?.stmtEntity?.statementIdLo ?: 0,
                )

            val verbLangMapEntries = schoolDb.getVerbLangMapEntryDao()
                .takeIf { !idOnly }
                ?.findByVerbUidPair(
                    uid1 = entity.verbEntity?.verbUid ?: 0,
                    uid2 = substatementEntity?.verbEntity?.verbUid ?: 0,
                ) ?: emptyList()

            val verbs = listOfNotNull(
                entity.verbEntity, substatementEntity?.verbEntity,
            ).map { verbEntity ->
                VerbEntities(
                    verbEntity = verbEntity,
                    verbLangMapEntries = verbLangMapEntries.filter {
                        it.vlmeVerbUid == verbEntity.verbUid
                    }
                ).toModel()
            }

            val allActorUids = entity.stmtEntity.allActorUids().appendIfNotNull(
                substatementEntity?.stmtEntity?.allActorUids()
            )

            val actorEntities = schoolDb.getActorDao().findByUidList(
                uids = allActorUids
            )

            val groupMemberJoins = schoolDb.getGroupMemberActorJoinDao().findByGroupActorUidList(
                uidList = allActorUids,
                excludeIdentifiedGroups = idOnly,
            )

            val actors = actorEntities.filter {
                //Filter out agents that are referenced by being group members
                it.actorUid in allActorUids
            }.distinctBy {
                it.actorUid
            }.map { actorEntity ->
                //Passing through all actor entitie is harmless because the ActorEntities.toModel
                //will use the group member joins to select the actor entity for an agent member of
                //the group
                ActorEntities(
                    actor = actorEntity,
                    groupMemberJoins = groupMemberJoins.filter {
                        it.gmajGroupActorUid == actorEntity.actorUid
                    },
                    groupMemberAgents = actorEntities,
                ).toModel(idOnlyFormat = idOnly)
            }

            val activities = schoolDb.getActivityEntityDao()
                .takeIf { !idOnly }
                ?.findByUidList(
                    entity.stmtEntity.allActivityUids(contextActivityJoins).appendIfNotNull(
                        substatementEntity?.stmtEntity?.allActivityUids(contextActivityJoins)
                    )
                )?.map { it.toModel(json) } ?: emptyList()

            StatementEntities(
                statements = listOfNotNull(
                    entity.stmtEntity, substatementEntity?.stmtEntity
                ),
                statementEntityJson = emptyList(),
                statementContextActivityJoins = contextActivityJoins,
            ).toModel(
                json = json,
                statementIdHi = entity.stmtEntity.statementIdHi,
                statementIdLo = entity.stmtEntity.statementIdLo,
                uidNumberMapper = uidNumberMapper,
                verbs = verbs,
                actors = actors,
                activities = activities
            )
        }
    }


    private fun makeXapiHeadersForStatementResult(
        statements: List<XapiStatement>,
        consistentThrough: Instant,
        ascendingOrder: Boolean,
    ) = buildHeaders {
        set(
            name = HttpHeaders.LastModified,
            value = statements.lastModifiedGMTStringForRetrievedStatements(
                ascendingOrder, consistentThrough
            )
        )

        set(HEADER_XAPI_VERSION, xapiVersion)
        set(
            name = HEADER_XAPI_CONSISTENT_THROUGH,
            value = GMTDate(consistentThrough.toEpochMilliseconds()).toString()
        )
    }

    override suspend fun post(list: List<XapiStatement>): List<Uuid> {
        val statementsWithIdsSet = list.map {
            it.copyWithIdIfNotSet()
        }

        schoolDb.useWriterConnection { con ->
            con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                statementsWithIdsSet.forEach { statement ->
                    //check if this is a voiding statement
                    if(statement.verb.id == XapiVerb.ID_VOIDED) {
                        //find the statement we are going to void
                        val statementRef = statement.`object` as? XapiStatementRef
                            ?: throw XapiBadRequestException("Voiding statement object not statementref")
                        val (voidIdHi, voidIdLo) = Uuid.parse(statementRef.id).toLongPair()

                        val verbUidToVoid = schoolDb.getStatementDao().getVerbUidNumToBeVoided(
                            statementIdHi = voidIdHi,
                            statementIdLo = voidIdLo,
                        ) ?: throw XapiForbiddenException("Statement to void not found")

                        if(uidNumberMapper(XapiVerb.ID_VOIDED) == verbUidToVoid)
                            throw XapiForbiddenException("Cannot void a void statement")

                        schoolDb.getStatementDao().updateSetStatementVoided(
                            voidIdHi, voidIdLo
                        )
                    }

                    doUpsertStatement(statement)
                }
            }
        }

        return statementsWithIdsSet.mapNotNull { it.id }
    }

    override suspend fun updateLocal(
        list: List<XapiStatement>,
        forceOverwrite: Boolean
    ) {
        //needs to check for existing statement, if existing, do nothing.
        schoolDb.useWriterConnection { con ->
            val storedTime = Clock.System.now()

            con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                list.map {
                    it.copy(stored = storedTime)
                }.forEach { statement ->
                    val (stmtIdHi, stmtIdLo) = (statement.id?.toLongPair()
                        ?: throw IllegalArgumentException("statement to store must have ids"))
                    val timesInDb = schoolDb.getStatementDao().getTimestampsByUuid(
                        statementIdHi = stmtIdHi,
                        statementIdLo = stmtIdLo,
                    )

                    //Because statements are immutable, if it is already in the db, do NOTHING.
                    if(timesInDb == null) {
                        doUpsertStatement(statement)
                    }
                }
            }
        }
    }

    override suspend fun get(
        listParams: GetStatementParams,
        dataLoadParams: DataLoadParams,
    ): DataLoadState<XapiStatementResult> {

        val statementIds = listParams.statementId?.toLongPair()
        val voidedStatementIds = listParams.voidedStatementId?.toLongPair()
        val format = listParams.format ?: XapiStatementsResource.GetStatementFormatEnum.EXACT
        val ascendingOrder = listParams.ascending
        val consistentThrough = Clock.System.now()

        return schoolDb.useReaderConnection { con ->
            con.withTransaction(Transactor.SQLiteTransactionType.DEFERRED) {
                val statements =  if(format == XapiStatementsResource.GetStatementFormatEnum.EXACT) {
                    schoolDb.getStatementEntityJsonDao().list(
                        statementIdHi = statementIds?.first ?: 0,
                        statementIdLo = statementIds?.second ?: 0,
                        voidedStatementIdHi = voidedStatementIds?.first ?: 0,
                        voidedStatementIdLo = voidedStatementIds?.second ?: 0,
                        agentUid = listParams.agent?.identifierHash(uidNumberMapper) ?: 0,
                        verbUid = listParams.verb?.let { uidNumberMapper(it) } ?: 0,
                        activityUid = listParams.activity?.let { uidNumberMapper(it) } ?: 0,
                        relatedAgents = listParams.relatedAgents,
                        relatedActivities = listParams.relatedActivities,
                        since = listParams.since?.toEpochMilliseconds() ?: XapiStatementEntityDao.SINCE_UNSET,
                        until = listParams.until?.toEpochMilliseconds() ?: XapiStatementEntityDao.UNTIL_UNSET,
                        ascending = ascendingOrder,
                        limit = listParams.limit ?: DEFAULT_MAX_STATEMENTS,
                    ).map { entity ->
                        json.decodeFromString(
                            XapiStatementTransformingSerializer, entity.fullStatement
                        )
                    }
                }else {
                    schoolDb.getStatementDao().list(
                        statementIdHi = statementIds?.first ?: 0,
                        statementIdLo = statementIds?.second ?: 0,
                        voidedStatementIdHi = voidedStatementIds?.first ?: 0,
                        voidedStatementIdLo = voidedStatementIds?.second ?: 0,
                        agentUid = listParams.agent?.identifierHash(uidNumberMapper) ?: 0,
                        verbUid = listParams.verb?.let { uidNumberMapper(it) } ?: 0,
                        activityUid = listParams.activity?.let { uidNumberMapper(it) } ?: 0,
                        relatedAgents = listParams.relatedAgents,
                        relatedActivities = listParams.relatedActivities,
                        since = listParams.since?.toEpochMilliseconds() ?: XapiStatementEntityDao.SINCE_UNSET,
                        until = listParams.until?.toEpochMilliseconds() ?: XapiStatementEntityDao.UNTIL_UNSET,
                        ascending = ascendingOrder,
                        limit = listParams.limit ?: DEFAULT_MAX_STATEMENTS,
                    ).mapToCanonicalStatements(
                        idOnly = format == XapiStatementsResource.GetStatementFormatEnum.IDS
                    )
                }

                DataReadyState(
                    data = XapiStatementResult(
                        statements = statements,
                        more = null,
                    ),
                    metaInfo = DataLoadMetaInfo(
                        headers = makeXapiHeadersForStatementResult(
                            statements, consistentThrough, ascendingOrder
                        )
                    )
                )
            }
        }
    }

    override fun getAsFlow(
        listParams: GetStatementParams,
        dataLoadParams: DataLoadParams
    ): Flow<DataLoadState<XapiStatementResult>> {
        val statementIds = listParams.statementId?.toLongPair()
        val voidedStatementIds = listParams.voidedStatementId?.toLongPair()
        val format = listParams.format ?: XapiStatementsResource.GetStatementFormatEnum.EXACT

        return if(format == XapiStatementsResource.GetStatementFormatEnum.EXACT) {
            schoolDb.getStatementEntityJsonDao().listAsFlow(
                statementIdHi = statementIds?.first ?: 0,
                statementIdLo = statementIds?.second ?: 0,
                voidedStatementIdHi = voidedStatementIds?.first ?: 0,
                voidedStatementIdLo = voidedStatementIds?.second ?: 0,
                agentUid = listParams.agent?.identifierHash(uidNumberMapper) ?: 0,
                verbUid = listParams.verb?.let { uidNumberMapper(it) } ?: 0,
                activityUid = listParams.activity?.let { uidNumberMapper(it) } ?: 0,
                relatedAgents = listParams.relatedAgents,
                relatedActivities = listParams.relatedActivities,
                since = listParams.since?.toEpochMilliseconds() ?: XapiStatementEntityDao.SINCE_UNSET,
                until = listParams.until?.toEpochMilliseconds() ?: XapiStatementEntityDao.UNTIL_UNSET,
                ascending = listParams.ascending,
                limit = listParams.limit ?: DEFAULT_MAX_STATEMENTS,
            ).map { list ->
                DataReadyState(
                    data = XapiStatementResult(
                        statements = list.map { entity ->
                            json.decodeFromString(
                                XapiStatementTransformingSerializer, entity.fullStatement
                            )
                        },
                        more = null
                    )
                )
            }
        }else {
            schoolDb.getStatementDao().listAsFlow(
                statementIdHi = statementIds?.first ?: 0,
                statementIdLo = statementIds?.second ?: 0,
                voidedStatementIdHi = voidedStatementIds?.first ?: 0,
                voidedStatementIdLo = voidedStatementIds?.second ?: 0,
                agentUid = listParams.agent?.identifierHash(uidNumberMapper) ?: 0,
                verbUid = listParams.verb?.let { uidNumberMapper(it) } ?: 0,
                activityUid = listParams.activity?.let { uidNumberMapper(it) } ?: 0,
                relatedAgents = listParams.relatedAgents,
                relatedActivities = listParams.relatedActivities,
                since = listParams.since?.toEpochMilliseconds() ?: XapiStatementEntityDao.SINCE_UNSET,
                until = listParams.until?.toEpochMilliseconds() ?: XapiStatementEntityDao.UNTIL_UNSET,
                ascending = listParams.ascending,
                limit = listParams.limit ?: DEFAULT_MAX_STATEMENTS,
            ).map { list ->
                val consistentThrough = Clock.System.now()

                val statements = list.mapToCanonicalStatements(
                    idOnly = format == XapiStatementsResource.GetStatementFormatEnum.IDS
                )
                DataReadyState(
                    data = XapiStatementResult(
                        statements = statements,
                        more = null,
                    ),
                    metaInfo = DataLoadMetaInfo(
                        headers = makeXapiHeadersForStatementResult(
                            statements, consistentThrough, listParams.ascending,
                        )
                    )
                )
            }
        }
    }

    override suspend fun getByUuid(uuid: Uuid): XapiStatement? {
        return get(
            listParams = GetStatementParams(statementId = uuid)
        ).dataOrNull()?.statements?.firstOrNull()
    }

    override suspend fun findByUidList(uids: List<String>): List<XapiStatement> {
        TODO()
    }

    override fun getAssignmentProgress(
        activityId: String,
        filterByActor: XapiActor?,
    ): Flow<DataLoadState<AssignmentAndProgress>> {
        //Get the statement itself to get the actor and list of assigned activities.
        return getAsFlow(
            listParams = GetStatementParams(
                activity = activityId,
                relatedActivities = false,
                format = XapiStatementsResource.GetStatementFormatEnum.CANONICAL,
            ),
            dataLoadParams = DataLoadParams(),
        ).map { statementResult ->
            val assignmentStatement = statementResult.dataOrNull()?.statements
                ?.mostRecentByTimestampOrNull()

            if(assignmentStatement == null) {
                NoDataLoadedState(reason = NoDataLoadedState.Reason.NOT_FOUND)
            }else {
                val assignedActivities = assignmentStatement.context?.contextActivities?.grouping
                    ?: emptyList()

                val actorsToShow = when(val statementActor = assignmentStatement.actor){
                    is XapiAgent -> listOf(statementActor)
                    is XapiGroup -> statementActor.member ?: throw IllegalStateException(
                        "getAssignmentResults: XapiGroup should include members when retrieved using canonical format"
                    )
                }

                val dbAssignmentResults = schoolDb.getStatementDao().getAssignmentResults(
                    uidNumberMapper(activityId)
                ).groupBy { it.actorUid }

                DataReadyState(
                    data = AssignmentAndProgress(
                        assignmentStatement = assignmentStatement,
                        progress = actorsToShow.map { actor ->
                            val actorUidNum = actor.identifierHash(uidNumberMapper)
                            XapiActorAndAssignmentProgress(
                                actor = actor,
                                progress = assignedActivities.map { taskActivity ->
                                    dbAssignmentResults[actorUidNum]?.firstOrNull {
                                        it.activityUid == uidNumberMapper(taskActivity.id)
                                    }?.toXapiAssignmentResult(taskActivity.id)
                                        ?: XapiAssignmentProgress.emptyResult(taskActivity.id)
                                }
                            )
                        }
                    )
                )
            }
        }
    }

    companion object {
        const val DEFAULT_MAX_STATEMENTS = 5_000

        const val XAPI_VERSION = "1.0.3"

    }

}