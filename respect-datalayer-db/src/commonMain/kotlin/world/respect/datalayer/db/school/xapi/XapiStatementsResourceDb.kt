package world.respect.datalayer.db.school.xapi

import androidx.room.Transactor
import androidx.room.useReaderConnection
import androidx.room.useWriterConnection
import io.ktor.http.Url
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
import world.respect.datalayer.school.xapi.XapiActorDataSourceLocal
import world.respect.lib.xapi.resources.XapiStatementsResource.GetStatementsRequest
import world.respect.datalayer.school.xapi.ext.allActors
import world.respect.datalayer.school.xapi.ext.allDefinedVerbs
import world.respect.datalayer.school.xapi.ext.distinctMerged
import world.respect.datalayer.school.xapi.ext.copyWithIdIfNotSet
import world.respect.lib.xapi.XapiRequestHeaders
import world.respect.lib.xapi.XapiResponseHeaders
import world.respect.lib.xapi.model.XapiStatementResult
import world.respect.lib.xapi.model.XapiStatementTransformingSerializer

class XapiStatementsResourceDb(
    private val schoolDb: RespectSchoolDatabase,
    private val authenticatedUser: AuthenticatedUserPrincipalId,
    private val schoolUrl: Url,
    private val uidNumberMapper: UidNumberMapper,
    private val json: Json,
    private val xapiActivityDataSourceLocal: XapiActivityDataSourceLocal,
    private val xapiActorDataSourceLocal: XapiActorDataSourceLocal,
) : XapiStatementsResourceLocal{

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


    override suspend fun post(list: List<XapiStatement>): List<Uuid> {
        val statementsWithIdsSet = list.map {
            it.copyWithIdIfNotSet()
        }

        schoolDb.useWriterConnection { con ->
            con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                statementsWithIdsSet.forEach { statement ->
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
                        ?: throw IllegalArgumentException("statement to store must have timestamps"))
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
        request: GetStatementsRequest,
    ): XapiStatementsResource.GetStatementsResponse {

        val statementIds = request.params.statementId?.toLongPair()
        val format = request.params.format ?: XapiStatementsResource.GetStatementFormatEnum.EXACT
        val ascendingOrder = request.params.ascending ?: false

        return schoolDb.useReaderConnection { con ->
            con.withTransaction(Transactor.SQLiteTransactionType.DEFERRED) {
                val statements =  if(format == XapiStatementsResource.GetStatementFormatEnum.EXACT) {
                    schoolDb.getStatementEntityJsonDao().list(
                        statementIdHi = statementIds?.first ?: 0,
                        statementIdLo = statementIds?.second ?: 0,
                        agentUid = request.params.agent?.identifierHash(uidNumberMapper) ?: 0,
                        verbUid = request.params.verb?.let { uidNumberMapper(it) } ?: 0,
                        activityUid = request.params.activity?.let { uidNumberMapper(it) } ?: 0,
                        relatedAgents = request.params.relatedAgents,
                        relatedActivities = request.params.relatedActivities,
                        since = request.params.since?.toEpochMilliseconds() ?: XapiStatementEntityDao.SINCE_UNSET,
                        until = request.params.since?.toEpochMilliseconds() ?: XapiStatementEntityDao.UNTIL_UNSET,
                        ascending = ascendingOrder,
                        limit = request.params.limit ?: DEFAULT_MAX_STATEMENTS,
                    ).map { entity ->
                        json.decodeFromString(
                            XapiStatementTransformingSerializer, entity.fullStatement
                        )
                    }
                }else {
                    val idOnly = format == XapiStatementsResource.GetStatementFormatEnum.IDS

                    schoolDb.getStatementDao().list(
                        statementIdHi = statementIds?.first ?: 0,
                        statementIdLo = statementIds?.second ?: 0,
                        agentUid = request.params.agent?.identifierHash(uidNumberMapper) ?: 0,
                        verbUid = request.params.verb?.let { uidNumberMapper(it) } ?: 0,
                        activityUid = request.params.activity?.let { uidNumberMapper(it) } ?: 0,
                        relatedAgents = request.params.relatedAgents,
                        relatedActivities = request.params.relatedActivities,
                        since = request.params.since?.toEpochMilliseconds() ?: XapiStatementEntityDao.SINCE_UNSET,
                        until = request.params.since?.toEpochMilliseconds() ?: XapiStatementEntityDao.UNTIL_UNSET,
                        ascending = ascendingOrder,
                        limit = request.params.limit ?: DEFAULT_MAX_STATEMENTS,
                    ).map { entity ->
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
                                uid1 = entity.verbEntity.verbUid,
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


                XapiStatementsResource.GetStatementsResponse(
                    statementResult = XapiStatementResult(
                        statements = statements,
                        more = null,
                    ),
                    headers = XapiResponseHeaders(
                        lastModified = if(ascendingOrder && statements.isNotEmpty()) {
                            statements.last().stored ?: throw IllegalStateException("Stored statement must have stored set")
                        }else if(!ascendingOrder && statements.isNotEmpty()) {
                            statements.first().stored ?: throw IllegalStateException("Stored statement must have stored set")
                        }else {
                            Clock.System.now()
                        }
                    )
                )
            }
        }
    }

    override suspend fun getByUuid(uuid: Uuid): XapiStatement? {
        return get(
            GetStatementsRequest(
                params = XapiStatementsResource.GetStatementParams(
                    statementId = uuid
                ),
                headers = XapiRequestHeaders()
            )
        ).statementResult.statements.firstOrNull()
    }

    override suspend fun findByUidList(uids: List<String>): List<XapiStatement> {
        TODO()
    }

    companion object {
        const val DEFAULT_MAX_STATEMENTS = 5_000
    }
}