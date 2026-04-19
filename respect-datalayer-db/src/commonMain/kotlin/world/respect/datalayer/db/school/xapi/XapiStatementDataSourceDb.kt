package world.respect.datalayer.db.school.xapi

import io.ktor.http.Url
import kotlinx.serialization.json.Json
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.ext.toLongPair
import world.respect.datalayer.db.school.xapi.adapters.StatementEntities
import world.respect.datalayer.db.school.xapi.adapters.toEntities
import world.respect.datalayer.school.xapi.XapiActivityDataSourceLocal
import world.respect.datalayer.school.xapi.XapiStatementDataSource
import world.respect.datalayer.school.xapi.XapiStatementDataSourceLocal
import world.respect.datalayer.school.xapi.ext.allDefinedActivities
import world.respect.datalayer.school.xapi.model.XapiAccount
import world.respect.datalayer.school.xapi.model.XapiAgent
import world.respect.datalayer.school.xapi.model.XapiStatement
import world.respect.lib.primarykeygen.PrimaryKeyGenerator
import kotlin.time.Clock
import kotlin.uuid.Uuid
import world.respect.datalayer.db.school.xapi.entities.StatementEntityObjectTypeEnum
import world.respect.datalayer.school.xapi.XapiActorDataSourceLocal
import world.respect.datalayer.school.xapi.ext.allActors

class XapiStatementDataSourceDb(
    private val schoolDb: RespectSchoolDatabase,
    private val authenticatedUser: AuthenticatedUserPrincipalId,
    private val schoolUrl: Url,
    private val uidNumberMapper: UidNumberMapper,
    private val primaryKeyGenerator: PrimaryKeyGenerator,
    private val json: Json,
    private val xapiActivityDataSourceLocal: XapiActivityDataSourceLocal,
    private val xapiActorDataSourceLocal: XapiActorDataSourceLocal,
) : XapiStatementDataSource, XapiStatementDataSourceLocal{

    suspend fun doUpsertStatement(
        stmt: XapiStatement
    ) {
        val timeNow = Clock.System.now()

        val stmtTimestamp = stmt.timestamp ?: timeNow

        val exactStatement = stmt.copy(
            stored = timeNow,
            timestamp = stmtTimestamp,
            id = stmt.id ?: Uuid.random(),
            authority = XapiAgent(
                account = XapiAccount(
                    name = authenticatedUser.guid,
                    homePage = schoolUrl.toString(),
                )
            )
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

        val activities = stmt.allDefinedActivities()
        xapiActivityDataSourceLocal.updateLocal(activities, stmtTimestamp)
        xapiActorDataSourceLocal.updateLocal(
            actors = stmt.allActors(),
            timestamp = stmtTimestamp,
        )

        /*
        statementEntity.actorEntities.map { it.actor }
            .filter { it.actorObjectType == ActorEntityTypeEnum.AGENT }
            .takeIf { it.isNotEmpty() }
            ?.also { agents ->
                //Name is the only property that could be updated on the Agent. All other
                //properties are identifiers
                schoolDb.getActorDao().insertOrUpdateActorsIfNameChanged(agents)
            }

        //Handle groups
        statementEntity.actorEntities.filter {
            it.actor.actorObjectType == ActorEntityTypeEnum.GROUP
        }.distinctBy {
            it.actor.actorUid
        }.forEach { group ->
            val existingGroupEntity = schoolDb.getActorDao().findByUidAsync(group.actor.actorUid)
            if((existingGroupEntity?.actorLastModified ?: EPOCH) > stmtTimestamp) {
                schoolDb.getGroupMemberActorJoinDao().deleteByGroupActorUidAsync(
                    group.actor.actorUid
                )
                schoolDb.getGroupMemberActorJoinDao().upsertListAsync(
                    group.groupMemberJoins
                )
                schoolDb.getActorDao().insertOrUpdateActorsIfNameChanged(
                    group.groupMemberAgents
                )
            }
        }

        schoolDb.getVerbDao().insertOrIgnoreAsync(
            statementEntity.verbEntities.map { it.verbEntity }
        )

        schoolDb.getVerbLangMapEntryDao().upsertList(
            statementEntity.verbEntities.flatMap { it.verbLangMapEntries }
        )

         */
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

        val statementEntities = schoolDb.getStatementDao().list(
            statementIdHi = statementIds?.first ?: 0,
            statementIdLo = statementIds?.second ?: 0,
        ).map { entity ->
            val substatementEntity = schoolDb.takeIf {
                entity.stmtEntity.statementObjectType == StatementEntityObjectTypeEnum.SUBSTATEMENT
            }?.getStatementDao()?.getEntityForSubstatement(
                subStatementIdHi = entity.stmtEntity.statementObjectUid1,
                subStatementIdLo = entity.stmtEntity.statementObjectUid2,
            )

            //now get StatementContextActivityJoins
            val contextActivityJoins = schoolDb.getStatementContextActivityJoinDao()
                .findAllByStatementId(
                    statementIdHi = entity.stmtEntity.statementIdHi,
                    statementIdLo = entity.stmtEntity.statementIdLo,
                )

            StatementEntities(
                statements = buildList {
                    add(entity.stmtEntity)
                    substatementEntity?.also { add(it) }
                },
                statementEntityJson = listOf(entity.stmtJsonEntity),
                statementContextActivityJoins = contextActivityJoins,
            )
        }

        TODO()
    }

    override suspend fun findByUidList(uids: List<String>): List<XapiStatement> {
        TODO()
    }
}