package world.respect.server.domain.school.add

import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent
import world.respect.datalayer.SchoolDataSourceLocal
import world.respect.datalayer.school.model.Person
import world.respect.datalayer.school.model.PersonRole
import world.respect.datalayer.schooldirectory.SchoolDirectoryDataSourceLocal
import world.respect.datalayer.respect.model.SchoolDirectoryEntry
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.datalayer.school.model.PersonGenderEnum
import world.respect.datalayer.school.model.PersonRoleEnum
import world.respect.datalayer.schooldirectory.SchoolDirectoryEntryDataSourceLocal
import world.respect.server.SchoolConfig
import world.respect.shared.domain.account.RespectAccount
import world.respect.shared.domain.account.setpassword.EncryptPersonPasswordUseCase
import world.respect.shared.util.di.RespectAccountScopeId
import world.respect.shared.util.di.SchoolDirectoryEntryScopeId

/**
 * Used by command line client, potentially web admin UI to add a realm.
 */
class AddSchoolUseCase(
    private val directoryDataSource: SchoolDirectoryDataSourceLocal,
    private val schoolDirectoryEntryDataSource: SchoolDirectoryEntryDataSourceLocal,
    private val encryptPasswordUseCase: EncryptPersonPasswordUseCase,
    private val schoolConfig: SchoolConfig
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

        // Check if school registration is enabled
        if (!schoolConfig.registration.enabled) {
            throw SchoolRegistrationDisabledException("New school registration is disabled")
        }

        // For subdomain mode, validate the domain matches the configured top-level domain
        if (schoolConfig.registration.mode == SchoolConfig.RegistrationConfig.RegistrationMode.SUBDOMAIN) {
            requests.forEach { request ->
                val schoolHost = request.school.self.host
                if (!schoolHost.endsWith(".${schoolConfig.registration.topLevelDomain}")) {
                    throw InvalidSchoolDomainException(
                        "School domain '$schoolHost' must end with '${schoolConfig.registration.topLevelDomain}'"
                    )
                }
            }
        }

        requests.forEach { request ->
            schoolDirectoryEntryDataSource.updateLocal(listOf(request.school))

            directoryDataSource.setServerManagedSchoolConfig(
                request.school,  request.dbUrl
            )

            val adminGuid = "1"
            val schoolScope = getKoin().createScope<SchoolDirectoryEntry>(
                SchoolDirectoryEntryScopeId(
                    request.school.self, null
                ).scopeId
            )

            val accountScope = getKoin().createScope<RespectAccount>(
                RespectAccountScopeId(
                    request.school.self, AuthenticatedUserPrincipalId(adminGuid)
                ).scopeId
            )

            accountScope.linkTo(schoolScope)

            val schoolDataSource: SchoolDataSourceLocal = accountScope.get()

            val adminPerson = Person(
                guid = "1",
                username = request.adminUsername,
                givenName = "Admin",
                familyName = "Admin",
                gender = PersonGenderEnum.UNSPECIFIED,
                roles = listOf(
                    PersonRole(
                        isPrimaryRole = true,
                        roleEnum = PersonRoleEnum.SYSTEM_ADMINISTRATOR,
                    )
                )
            )

            schoolDataSource.personDataSource.store(listOf(adminPerson))
            schoolDataSource.personPasswordDataSource.store(
                listOf(
                    encryptPasswordUseCase(
                        EncryptPersonPasswordUseCase.Request(
                            personGuid = adminPerson.guid,
                            password = request.adminPassword,
                        )
                    )
                )
            )
        }
    }

    companion object {
        const val DEFAULT_ADMIN_USERNAME = "admin"
    }

}

class SchoolRegistrationDisabledException(message: String) : Exception(message)
class InvalidSchoolDomainException(message: String) : Exception(message)