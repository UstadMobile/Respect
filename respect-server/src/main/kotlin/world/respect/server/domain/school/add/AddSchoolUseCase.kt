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
import world.respect.shared.domain.account.RespectAccount
import world.respect.shared.domain.account.setpassword.SetPasswordUseCase
import world.respect.shared.util.di.RespectAccountScopeId
import world.respect.shared.util.di.SchoolDirectoryEntryScopeId

/**
 * Used by command line client, potentially web admin UI to add a realm.
 */
class AddSchoolUseCase(
    private val directoryDataSource: SchoolDirectoryDataSourceLocal,
    private val schoolDirectoryEntryDataSource: SchoolDirectoryEntryDataSourceLocal,
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
            schoolDirectoryEntryDataSource.updateLocal(
                listOf(
                    request.school.copy(
                        directoryCode = directoryDataSource.getServerManagedDirectory()?.invitePrefix
                    )
                )
            )

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
            val setPasswordUseCase: SetPasswordUseCase = accountScope.get()

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