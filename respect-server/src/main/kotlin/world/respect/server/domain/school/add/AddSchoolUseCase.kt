package world.respect.server.domain.school.add

import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent
import world.respect.datalayer.SchoolDataSourceLocal
import world.respect.datalayer.school.model.Person
import world.respect.datalayer.school.model.PersonRole
import world.respect.datalayer.schooldirectory.SchoolDirectoryDataSourceLocal
import world.respect.datalayer.respect.model.SchoolDirectoryEntry
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.shared.domain.account.setpassword.SetPasswordUseCase
import world.respect.shared.util.di.SchoolDirectoryEntryScopeId
import kotlin.time.ExperimentalTime

/**
 * Used by command line client, potentially web admin UI to add a realm.
 */
@OptIn(ExperimentalTime::class)
class AddSchoolUseCase(
    private val directoryDataSource: SchoolDirectoryDataSourceLocal
): KoinComponent {

    @Serializable
    data class AddSchoolRequest(
        val school: SchoolDirectoryEntry,
        val dbUrl: String,
        val adminUsername: String,
        val adminPassword: String,
    )

    suspend operator fun invoke(
        requests: List<AddSchoolRequest>
    ) {
        requests.forEach { request ->
            directoryDataSource.addServerManagedSchool(
                request.school,  request.dbUrl
            )

            val schoolScope = getKoin().createScope<SchoolDirectoryEntry>(
                SchoolDirectoryEntryScopeId(
                    request.school.self, null
                ).scopeId
            )
            val schoolDataSource: SchoolDataSourceLocal = schoolScope.get()
            val setPasswordUseCase: SetPasswordUseCase = schoolScope.get()

            val adminPerson = Person(
                guid = "1",
                username = request.adminUsername,
                givenName = "Admin",
                familyName = "Admin",
                roles = listOf(
                    PersonRole(
                        isPrimaryRole = true,
                        roleType = PersonRole.RoleType.SYSTEM_ADMINISTRATOR,
                    )
                )
            )

            schoolDataSource.personDataSource.putPersonsLocal(listOf(adminPerson))

            setPasswordUseCase(
                SetPasswordUseCase.SetPasswordRequest(
                    authenticatedUserId = AuthenticatedUserPrincipalId.directoryAdmin,
                    userGuid = adminPerson.guid,
                    password = request.adminPassword,
                )
            )
        }
    }

    companion object {
        const val DEFAULT_ADMIN_USERNAME = "admin"
    }

}