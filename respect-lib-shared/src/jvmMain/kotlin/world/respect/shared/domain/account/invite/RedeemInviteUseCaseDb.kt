package world.respect.shared.domain.account.invite

import io.ktor.http.Url
import org.koin.core.component.KoinComponent
import world.respect.credentials.passkey.RespectRedeemInviteRequest
import world.respect.credentials.passkey.RespectRedeemInviteRequest.RedeemInviteCredential
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.school.model.Person
import world.respect.datalayer.school.model.PersonRole
import world.respect.datalayer.school.model.PersonRoleEnum
import world.respect.datalayer.school.model.PersonStatusEnum
import world.respect.libutil.util.throwable.withHttpStatusCode
import world.respect.shared.domain.account.AuthResponse
import world.respect.shared.domain.account.gettokenanduser.GetTokenAndUserProfileWithUsernameAndPasswordUseCase
import world.respect.shared.domain.account.setpassword.SetPasswordUseCase
import world.respect.shared.domain.school.SchoolPrimaryKeyGenerator
import world.respect.shared.util.di.SchoolDataSourceLocalProvider
import java.lang.IllegalArgumentException

/**
 * Server-side use case that
 */
class RedeemInviteUseCaseDb(
    private val schoolDb: RespectSchoolDatabase,
    private val uidNumberMapper: UidNumberMapper,
    private val schoolUrl: Url,
    private val schoolPrimaryKeyGenerator: SchoolPrimaryKeyGenerator,
    private val setPasswordUseCase: SetPasswordUseCase,
    private val getTokenAndUserProfileUseCase: GetTokenAndUserProfileWithUsernameAndPasswordUseCase,
    private val schoolDataSource: SchoolDataSourceLocalProvider,
): RedeemInviteUseCase, KoinComponent {

    fun RespectRedeemInviteRequest.PersonInfo.toPerson(
        role: PersonRoleEnum,
        username: String,
    ) : Person {
        return Person(
            guid =  schoolPrimaryKeyGenerator.primaryKeyGenerator.nextId(
                Person.TABLE_ID
            ).toString(),
            status = PersonStatusEnum.PENDING_APPROVAL,
            givenName = name.substringBeforeLast(" "),
            familyName = name.substringAfterLast(" "),
            username = username,
            gender = gender,
            roles = listOf(
                PersonRole(
                    isPrimaryRole = true,
                    roleEnum = role,
                )
            )
        )
    }

    override suspend fun invoke(
        redeemRequest: RespectRedeemInviteRequest
    ): AuthResponse {
        val classUid = redeemRequest.classUid
            ?: throw IllegalArgumentException("No class guid").withHttpStatusCode(400)
        val clazz = schoolDb.getClassEntityDao().findByGuid(uidNumberMapper(classUid))
            ?: throw IllegalArgumentException("Class not found").withHttpStatusCode(400)
        val expectedInviteCode = when(redeemRequest.role) {
            PersonRoleEnum.TEACHER -> clazz.cTeacherInviteCode
            else -> clazz.cStudentInviteCode
        } ?: throw IllegalArgumentException("No invite code for requested role")
            .withHttpStatusCode(400)

        if(!redeemRequest.code.endsWith(expectedInviteCode)) {
            throw IllegalArgumentException("Bad code").withHttpStatusCode(400)
        }

        val accountGuid = schoolPrimaryKeyGenerator.primaryKeyGenerator.nextId(
            Person.TABLE_ID
        ).toString()

        val accountPerson = redeemRequest.accountPersonInfo.toPerson(
            redeemRequest.role, redeemRequest.account.username
        )

        schoolDataSource(
            schoolUrl = schoolUrl, AuthenticatedUserPrincipalId(accountGuid)
        ).personDataSource.updateLocal(listOf(accountPerson))

        val credential = redeemRequest.account.credential

        when(credential) {
            is RespectRedeemInviteRequest.RedeemInvitePasswordCredential ->{
                setPasswordUseCase(
                    SetPasswordUseCase.SetPasswordRequest(
                        authenticatedUserId = AuthenticatedUserPrincipalId(accountPerson.guid),
                        userGuid = accountPerson.guid,
                        password = credential.password,
                    )
                )

                return getTokenAndUserProfileUseCase(
                    username = redeemRequest.account.username,
                    password = credential.password,
                )
            }
            else -> {
                TODO("set credential passkey")
            }
        }



        //If a teacher/student, make the enrollment now
    }
}