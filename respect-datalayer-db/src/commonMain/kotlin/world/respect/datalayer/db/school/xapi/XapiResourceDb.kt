package world.respect.datalayer.db.school.xapi


import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.GetAuthenticatedPersonUseCase
import world.respect.datalayer.db.school.xapi.entities.XapiStatementEntity
import world.respect.datalayer.school.domain.CheckPersonPermissionUseCase
import world.respect.datalayer.school.xapi.XapiActivitiesResourceLocal
import world.respect.datalayer.school.xapi.XapiAgentsResourceLocal
import world.respect.datalayer.school.xapi.XapiLocalInvalidation
import world.respect.datalayer.school.xapi.XapiResourceLocal
import world.respect.datalayer.school.xapi.XapiStatementsResourceLocal

class XapiResourceDb(
    private val schoolDb: RespectSchoolDatabase,
    private val uidNumberMapper: UidNumberMapper,
    private val authenticatedUser: AuthenticatedUserPrincipalId,
    private val checkPersonPermissionUseCase: CheckPersonPermissionUseCase,
    private val json: Json,
    private val schoolUrl: Url,
): XapiResourceLocal {

    override val invalidationFlow: Flow<XapiLocalInvalidation>
        get() = schoolDb.invalidationTracker.createFlow("XapiStatementEntity").map {
            XapiLocalInvalidation(XapiStatementEntity::class)
        }

    private val getAuthenticatedPersonUseCase by lazy {
        GetAuthenticatedPersonUseCase(
            authenticatedUser, schoolDb, uidNumberMapper
        )
    }

    override val statements: XapiStatementsResourceLocal by lazy {
        XapiStatementsResourceDb(
            schoolDb = schoolDb,
            authenticatedUser = authenticatedUser,
            getAuthenticatedPersonUseCase = getAuthenticatedPersonUseCase,
            schoolUrl = schoolUrl,
            uidNumberMapper = uidNumberMapper,
            xapiActivitiesResourceLocal = activities,
            xapiAgentsResourceLocal = agents,
            json = json,
        )
    }

    override val agents: XapiAgentsResourceLocal by lazy {
        XapiAgentsResourceDb(
            schoolDb = schoolDb,
            authenticatedUser = authenticatedUser,
            uidNumberMapper = uidNumberMapper,
        )
    }

    override val activities: XapiActivitiesResourceLocal by lazy{
        XapiActivitiesResourceDb(
            schoolDb = schoolDb,
            authenticatedUser = authenticatedUser,
            uidNumberMapper = uidNumberMapper,
            json = json,
        )
    }

    override fun close() {
        schoolDb.close()
    }
}