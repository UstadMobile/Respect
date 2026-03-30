package world.respect.datalayer.db.school.xapi

import io.ktor.http.Url
import kotlinx.serialization.json.Json
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.domain.xapi.StoreActivitiesUseCase
import world.respect.datalayer.db.school.xapi.adapters.toEntities
import world.respect.datalayer.db.school.xapi.entities.XapiEntityObjectTypeFlags
import world.respect.datalayer.db.school.xapi.ext.insertOrUpdateActorsIfNameChanged
import world.respect.datalayer.ext.EPOCH
import world.respect.datalayer.school.xapi.XapiStatementDataSource
import world.respect.datalayer.school.xapi.XapiStatementDataSourceLocal
import world.respect.datalayer.school.xapi.model.XapiAccount
import world.respect.datalayer.school.xapi.model.XapiAgent
import world.respect.datalayer.school.xapi.model.XapiStatement
import world.respect.lib.primarykeygen.PrimaryKeyGenerator
import kotlin.time.Clock
import kotlin.uuid.Uuid

class XapiStatementDataSourceDb(
    private val schoolDb: RespectSchoolDatabase,
    private val authenticatedUser: AuthenticatedUserPrincipalId,
    private val schoolUrl: Url,
    private val uidNumberMapper: UidNumberMapper,
    private val primaryKeyGenerator: PrimaryKeyGenerator,
    private val json: Json,
    private val storeActivitiesUseCase: StoreActivitiesUseCase,
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
            primaryKeyGenerator = primaryKeyGenerator,
            json = json,
            exactJson = json.encodeToString(XapiStatement.serializer(), exactStatement),
            isSubStatement = false,
        )

        schoolDb.getStatementDao().insertOrIgnoreListAsync(statementEntity.statements)

        schoolDb.getStatementEntityJsonDao().insertOrIgnoreListAsync(
            statementEntity.statementEntityJson
        )

        statementEntity.actorEntities.map { it.actor }
            .filter { it.actorObjectType == XapiEntityObjectTypeFlags.AGENT }
            .takeIf { it.isNotEmpty() }
            ?.also { agents ->
                //Name is the only property that could be updated on the Agent. All other
                //properties are identifiers
                schoolDb.getActorDao().insertOrUpdateActorsIfNameChanged(agents)
            }

        //Handle groups
        statementEntity.actorEntities.filter {
            it.actor.actorObjectType == XapiEntityObjectTypeFlags.GROUP
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

        storeActivitiesUseCase(statementEntity.activityEntities)
    }


    override suspend fun store(list: List<XapiStatement>) {
        //TODO: permission checks
        list.forEach { statement ->
            doUpsertStatement(statement)
        }
    }

    override suspend fun updateLocal(
        list: List<XapiStatement>,
        forceOverwrite: Boolean
    ) {
        list.forEach { statement ->
            doUpsertStatement(statement)
        }
    }

    override suspend fun list(
        listParams: XapiStatementDataSource.GetStatementParams,
        dataLoadParams: DataLoadParams
    ): DataLoadState<List<XapiStatement>> {
        TODO()
    }

    override suspend fun findByUidList(uids: List<String>): List<XapiStatement> {
        TODO()
    }
}