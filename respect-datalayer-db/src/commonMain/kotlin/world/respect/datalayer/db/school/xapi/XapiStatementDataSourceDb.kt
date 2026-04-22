package world.respect.datalayer.db.school.xapi

import androidx.room.Transactor
import androidx.room.useReaderConnection
import io.ktor.http.Url
import kotlinx.serialization.json.Json
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.DataReadyState
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
import world.respect.datalayer.school.xapi.XapiStatementDataSource
import world.respect.datalayer.school.xapi.XapiStatementDataSourceLocal
import world.respect.datalayer.school.xapi.ext.allDefinedActivities
import world.respect.datalayer.school.xapi.model.XapiStatement
import world.respect.lib.primarykeygen.PrimaryKeyGenerator
import kotlin.time.Clock
import kotlin.uuid.Uuid
import world.respect.datalayer.db.school.xapi.entities.XapiStatementEntityObjectTypeEnum
import world.respect.datalayer.db.school.xapi.ext.allActivityUids
import world.respect.datalayer.db.school.xapi.ext.allActorUids
import world.respect.datalayer.ext.appendIfNotNull
import world.respect.datalayer.school.xapi.XapiActorDataSourceLocal
import world.respect.datalayer.school.xapi.ext.allActors
import world.respect.datalayer.school.xapi.ext.allDefinedVerbs
import world.respect.datalayer.school.xapi.ext.distinctMerged

class XapiStatementDataSourceDb(
    private val schoolDb: RespectSchoolDatabase,
    private val authenticatedUser: AuthenticatedUserPrincipalId,
    private val schoolUrl: Url,
    private val uidNumberMapper: UidNumberMapper,
    private val primaryKeyGenerator: PrimaryKeyGenerator,
    private val json: Json,
    private val xapiActivityDataSourceLocal: XapiActivityDataSourceLocal,
    private val xapiActorDataSourceLocal: XapiActorDataSourceLocal,
) : XapiStatementDataSourceLocal{

    suspend fun doUpsertStatement(
        stmt: XapiStatement
    ) {
        val timeNow = Clock.System.now()

        val stmtTimestamp = stmt.timestamp ?: timeNow

        val exactStatement = stmt.copy(
            stored = timeNow,
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


    override suspend fun store(list: List<XapiStatement>) {
        list.forEach { statement ->
            doUpsertStatement(statement)
        }
    }

    override suspend fun updateLocal(
        list: List<XapiStatement>,
        forceOverwrite: Boolean
    ) {
        //needs to check for existing statement, if existing, do nothing.


        list.forEach { statement ->
            doUpsertStatement(statement)
        }
    }

    override suspend fun list(
        listParams: XapiStatementDataSource.GetStatementParams,
        dataLoadParams: DataLoadParams
    ): DataLoadState<List<XapiStatement>> {

        val statementIds = listParams.statementId?.toLongPair()
        return schoolDb.useReaderConnection { con ->
            con.withTransaction(Transactor.SQLiteTransactionType.DEFERRED) {
                val statements =  schoolDb.getStatementDao().list(
                    statementIdHi = statementIds?.first ?: 0,
                    statementIdLo = statementIds?.second ?: 0,
                    agentUid = listParams.agent?.identifierHash(uidNumberMapper) ?: 0,
                    verbUid = listParams.verb?.let { uidNumberMapper(it) } ?: 0,
                    activityUid = listParams.activity?.let { uidNumberMapper(it) } ?: 0,
                    relatedAgents = listParams.relatedAgents,
                    relatedActivities = listParams.relatedActivities,
                    since = listParams.since?.toEpochMilliseconds() ?: XapiStatementEntityDao.SINCE_UNSET,
                    until = listParams.since?.toEpochMilliseconds() ?: XapiStatementEntityDao.UNTIL_UNSET,
                    ascending = listParams.ascending ?: false,
                    limit = listParams.limit ?: DEFAULT_MAX_STATEMENTS,
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

                    val verbLangMapEntries = schoolDb.getVerbLangMapEntryDao().findByVerbUidPair(
                        uid1 = entity.verbEntity.verbUid,
                        uid2 = substatementEntity?.verbEntity?.verbUid ?: 0,
                    )

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

                    val actorEntities = schoolDb.getActorDao().findByUidList(uids = allActorUids)
                    val groupMemberJoins = schoolDb.getGroupMemberActorJoinDao().findByGroupActorUidList(
                        uidList = allActorUids
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
                        ).toModel()
                    }

                    val activities = schoolDb.getActivityEntityDao().findByUidList(
                        entity.stmtEntity.allActivityUids(contextActivityJoins).appendIfNotNull(
                            substatementEntity?.stmtEntity?.allActivityUids(contextActivityJoins)
                        )
                    ).map { it.toModel(json) }

                    StatementEntities(
                        statements = listOfNotNull(
                            entity.stmtEntity, substatementEntity?.stmtEntity
                        ),
                        statementEntityJson = listOf(entity.stmtJsonEntity),
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

                DataReadyState(
                    data = statements,
                )
            }
        }
    }

    override suspend fun findByUidList(uids: List<String>): List<XapiStatement> {
        TODO()
    }

    companion object {
        const val DEFAULT_MAX_STATEMENTS = 5_000
    }
}