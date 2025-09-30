package world.respect.shared.domain.account.invite

import io.ktor.http.Url
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import world.respect.credentials.passkey.CreatePasskeyUseCase
import world.respect.credentials.passkey.RespectPasskeyCredential
import world.respect.credentials.passkey.RespectPasswordCredential
import world.respect.credentials.passkey.request.DecodeUserHandleUseCase
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.adapters.toEntity
import world.respect.datalayer.school.adapters.toPersonPasskey
import world.respect.datalayer.school.model.AuthToken
import world.respect.datalayer.school.model.Enrollment
import world.respect.datalayer.school.model.EnrollmentRoleEnum
import world.respect.datalayer.school.model.Person
import world.respect.datalayer.school.model.PersonRole
import world.respect.datalayer.school.model.PersonRoleEnum
import world.respect.datalayer.school.model.PersonStatusEnum
import world.respect.libutil.ext.randomString
import world.respect.libutil.util.throwable.withHttpStatus
import world.respect.shared.domain.account.AuthResponse
import world.respect.shared.domain.account.authwithpassword.GetTokenAndUserProfileWithCredentialDbImpl.Companion.TOKEN_DEFAULT_TTL
import world.respect.shared.domain.account.gettokenanduser.GetTokenAndUserProfileWithCredentialUseCase
import world.respect.shared.domain.account.setpassword.SetPasswordUseCase
import world.respect.shared.domain.school.SchoolPrimaryKeyGenerator
import world.respect.shared.util.di.SchoolDataSourceLocalProvider
import java.lang.IllegalArgumentException

/**
 * Server-side use case that handles redeeming an invite
 */
class RedeemInviteUseCaseDb(
    private val schoolDb: RespectSchoolDatabase,
    private val uidNumberMapper: UidNumberMapper,
    private val schoolUrl: Url,
    private val schoolPrimaryKeyGenerator: SchoolPrimaryKeyGenerator,
    private val setPasswordUseCase: SetPasswordUseCase,
    private val getTokenAndUserProfileUseCase: GetTokenAndUserProfileWithCredentialUseCase,
    private val schoolDataSource: SchoolDataSourceLocalProvider,
    private val json: Json,
    private val decodeUserHandle: DecodeUserHandleUseCase,
): RedeemInviteUseCase, KoinComponent {

    fun RespectRedeemInviteRequest.PersonInfo.toPerson(
        role: PersonRoleEnum,
        username: String,
        guid: String,
    ) : Person {
        return Person(
            guid =  guid,
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
            ?: throw IllegalArgumentException("No class guid").withHttpStatus(400)
        val clazz = schoolDb.getClassEntityDao().findByGuid(uidNumberMapper(classUid))
            ?: throw IllegalArgumentException("Class not found").withHttpStatus(400)
        val expectedInviteCode = when(redeemRequest.role) {
            PersonRoleEnum.TEACHER -> clazz.cTeacherInviteCode
            else -> clazz.cStudentInviteCode
        } ?: throw IllegalArgumentException("No invite code for requested role")
            .withHttpStatus(400)

        if(!redeemRequest.code.endsWith(expectedInviteCode)) {
            throw IllegalArgumentException("Bad code").withHttpStatus(400)
        }

        val accountGuid = schoolPrimaryKeyGenerator.primaryKeyGenerator.nextId(
            Person.TABLE_ID
        ).toString()

        val accountPerson = redeemRequest.accountPersonInfo.toPerson(
            role = redeemRequest.role,
            username = redeemRequest.account.username,
            guid = accountGuid,
        )

        val schoolDataSourceVal = schoolDataSource(
            schoolUrl = schoolUrl, AuthenticatedUserPrincipalId(accountGuid)
        )
        schoolDataSourceVal.personDataSource.updateLocal(listOf(accountPerson))

        val credential = redeemRequest.account.credential

        val authResponse = when(credential) {
            is RespectPasswordCredential ->{
                setPasswordUseCase(
                    SetPasswordUseCase.SetPasswordRequest(
                        authenticatedUserId = AuthenticatedUserPrincipalId(accountPerson.guid),
                        userGuid = accountPerson.guid,
                        password = credential.password,
                    )
                )

                getTokenAndUserProfileUseCase(credential)
            }

            is RespectPasskeyCredential -> {
                val userHandleEncoded = redeemRequest.account.userHandleEncoded
                    ?: throw kotlin.IllegalArgumentException("Passkey redeem requires a user handle")
                        .withHttpStatus(400)
                val userHandle = decodeUserHandle(userHandleEncoded)
                val passkeyCreatedResult = CreatePasskeyUseCase.PasskeyCreatedResult(
                    respectUserHandle = userHandle,
                    authenticationResponseJSON = credential.passkeyWebAuthNResponse,
                )

                schoolDataSourceVal.personPasskeyDataSource.store(
                    listOf(
                        passkeyCreatedResult.toPersonPasskey(
                            json = json,
                            personGuid = accountPerson.guid,
                            deviceName = redeemRequest.deviceName ?: "Unknown device type",
                        )
                    )
                )

                val token = AuthToken(
                    accessToken = randomString(32),
                    timeCreated = System.currentTimeMillis(),
                    ttl = TOKEN_DEFAULT_TTL,
                )

                val personGuidHash = uidNumberMapper(accountPerson.guid)
                schoolDb.getAuthTokenEntityDao().insert(
                    token.toEntity(accountPerson.guid, personGuidHash)
                )

                AuthResponse(
                    token = token,
                    person = accountPerson,
                )
            }
        }

        //If a teacher/student, make the pending enrollment now
        schoolDataSourceVal.enrollmentDataSource.store(
            listOf(
                Enrollment(
                    uid = schoolPrimaryKeyGenerator.primaryKeyGenerator.nextId(
                        Enrollment.TABLE_ID
                    ).toString(),
                    classUid = classUid,
                    personUid = accountPerson.guid,
                    role = if(redeemRequest.role == PersonRoleEnum.TEACHER) {
                        EnrollmentRoleEnum.PENDING_TEACHER
                    }else {
                        EnrollmentRoleEnum.PENDING_STUDENT
                    },
                    inviteCode = redeemRequest.code,
                )
            )
        )

        return authResponse
    }
}