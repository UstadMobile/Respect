package world.respect.server.domain.school.add

import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent
import world.respect.datalayer.SchoolDataSourceLocal
import world.respect.datalayer.school.model.Person
import world.respect.datalayer.school.model.PersonRole
import world.respect.datalayer.schooldirectory.SchoolDirectoryDataSourceLocal
import world.respect.datalayer.respect.model.SchoolDirectoryEntry
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.datalayer.school.model.PersonRoleEnum
import world.respect.shared.domain.account.RespectAccount
import world.respect.shared.domain.account.setpassword.SetPasswordUseCase
import world.respect.shared.util.di.RespectAccountScopeId
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
                roles = listOf(
                    PersonRole(
                        isPrimaryRole = true,
                        roleEnum = PersonRoleEnum.SYSTEM_ADMINISTRATOR,
                    )
                )
            )

            schoolDataSource.personDataSource.store(listOf(adminPerson))

            schoolDataSource.personDataSource.store(
                (2..300).map {
                    Person(
                        guid = "$it",
                        username = "user$it",
                        givenName = "Person$it",
                        familyName = "Lastname$it",
                        roles = listOf(
                            PersonRole(
                                isPrimaryRole = true,
                                roleEnum = PersonRoleEnum.SYSTEM_ADMINISTRATOR,
                            )
                        )
                    )
                }
            )

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