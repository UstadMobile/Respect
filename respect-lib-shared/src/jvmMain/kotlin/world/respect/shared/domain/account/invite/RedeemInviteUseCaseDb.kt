package world.respect.shared.domain.account.invite

import io.ktor.http.Url
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import world.respect.credentials.passkey.CreatePasskeyUseCase
import world.respect.credentials.passkey.RespectPasskeyCredential
import world.respect.credentials.passkey.RespectPasswordCredential
import world.respect.credentials.passkey.RespectUserHandle
import world.respect.credentials.passkey.request.GetPasskeyProviderInfoUseCase
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.adapters.toEntity
import world.respect.datalayer.school.adapters.toPersonPasskey
import world.respect.datalayer.school.model.AuthToken
import world.respect.datalayer.school.model.Enrollment
import world.respect.datalayer.school.model.EnrollmentRoleEnum
import world.respect.datalayer.school.model.Invite
import world.respect.datalayer.school.model.PersonRoleEnum
import world.respect.libutil.ext.randomString
import world.respect.libutil.util.throwable.withHttpStatus
import world.respect.libutil.util.time.systemTimeInMillis
import world.respect.shared.domain.account.AuthResponse
import world.respect.shared.domain.account.authwithpassword.GetTokenAndUserProfileWithCredentialDbImpl.Companion.TOKEN_DEFAULT_TTL
import world.respect.shared.domain.account.gettokenanduser.GetTokenAndUserProfileWithCredentialUseCase
import world.respect.shared.domain.account.setpassword.EncryptPersonPasswordUseCase
import world.respect.shared.domain.school.SchoolPrimaryKeyGenerator
import world.respect.shared.util.di.SchoolDataSourceLocalProvider
import world.respect.shared.util.toPerson
import java.lang.IllegalArgumentException

/**
 * Server-side use case that handles redeeming an invite
 */
class RedeemInviteUseCaseDb(
    private val schoolDb: RespectSchoolDatabase,
    private val uidNumberMapper: UidNumberMapper,
    private val schoolUrl: Url,
    private val schoolPrimaryKeyGenerator: SchoolPrimaryKeyGenerator,
    private val getTokenAndUserProfileUseCase: GetTokenAndUserProfileWithCredentialUseCase,
    private val schoolDataSource: SchoolDataSourceLocalProvider,
    private val json: Json,
    private val getPasskeyProviderInfoUseCase: GetPasskeyProviderInfoUseCase,
    private val encryptPersonPasswordUseCase: EncryptPersonPasswordUseCase,
): RedeemInviteUseCase, KoinComponent {

    override suspend fun invoke(
        redeemRequest: RespectRedeemInviteRequest
    ): AuthResponse {
        val invite = schoolDb.getInviteEntityDao().getInviteByInviteCode(redeemRequest.code)
            ?: throw IllegalArgumentException("invite not found for code: ${redeemRequest.code}")
                .withHttpStatus(404)
        if (invite.iInviteStatus == Invite.STATUS_REVOKED) {
            throw IllegalArgumentException("invite is revoked")
                .withHttpStatus(400)
        }
        if (invite.iInviteStatus != Invite.STATUS_PENDING) {
            throw IllegalArgumentException("invite is already used")
                .withHttpStatus(400)
        }
        if (invite.iExpiration < systemTimeInMillis()) {
            throw IllegalArgumentException("invite is expired")
                .withHttpStatus(400)
        }

        val isDirectJoin = invite.iForClassGuid == null &&
                invite.iForFamilyOfGuid == null

        val isClassInvite = invite.iForClassGuid != null

        val isFamilyInvite = invite.iForFamilyOfGuid != null

        when {
            isDirectJoin -> {
            }

            isClassInvite -> {
                val classUid = redeemRequest.invite.forClassGuid
                    ?: throw IllegalArgumentException("No class guid").withHttpStatus(400)
                val clazz = schoolDb.getClassEntityDao().findByGuid(uidNumberMapper(classUid))
                    ?: throw IllegalArgumentException("Class not found").withHttpStatus(400)
                val expectedInviteGuid = when (redeemRequest.role) {
                    PersonRoleEnum.TEACHER -> clazz.cTeacherInviteGuid
                    else -> clazz.cStudentInviteGuid
                } ?: throw IllegalArgumentException("No invite code for requested role")
                    .withHttpStatus(400)
                val clazzInvite = schoolDb.getInviteEntityDao().findByGuid(expectedInviteGuid)
                    ?: throw IllegalArgumentException("No invite found")
                        .withHttpStatus(400)
                if (!redeemRequest.code.endsWith(clazzInvite.iCode)) {
                    throw IllegalArgumentException("Bad code").withHttpStatus(400)
                }
            }

            isFamilyInvite -> {

            }
        }


        val accountGuid = redeemRequest.account.guid

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
            is RespectPasswordCredential -> {
                schoolDataSourceVal.personPasswordDataSource.store(
                    listOf(
                        encryptPersonPasswordUseCase(
                            EncryptPersonPasswordUseCase.Request(
                                personGuid = accountGuid,
                                password = credential.password,
                            )
                        )
                    )
                )

                getTokenAndUserProfileUseCase(credential)
            }

            is RespectPasskeyCredential -> {
                val passkeyCreatedResult = CreatePasskeyUseCase.PasskeyCreatedResult(
                    respectUserHandle = RespectUserHandle(
                        personUidNum = uidNumberMapper(accountGuid),
                        schoolUrl = schoolUrl
                    ),
                    authenticationResponseJSON = credential.passkeyWebAuthNResponse,
                    passkeyProviderInfo = getPasskeyProviderInfoUseCase(
                        credential.passkeyWebAuthNResponse.response.authenticatorData
                    )
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
                    token.toEntity(
                        pGuid = accountPerson.guid,
                        pGuidHash = personGuidHash,
                        deviceInfo = redeemRequest.deviceInfo,
                    )
                )

                AuthResponse(
                    token = token,
                    person = accountPerson,
                )
            }
        }

        //If a teacher/student, make the pending enrollment now
        if(isClassInvite){

            val classUid = redeemRequest.invite.forClassGuid
                ?: throw IllegalArgumentException("No class guid").withHttpStatus(400)

            if (redeemRequest.role == PersonRoleEnum.TEACHER || redeemRequest.role == PersonRoleEnum.STUDENT) {
                schoolDataSourceVal.enrollmentDataSource.store(
                    listOf(
                        Enrollment(
                            uid = schoolPrimaryKeyGenerator.primaryKeyGenerator.nextId(
                                Enrollment.TABLE_ID
                            ).toString(),
                            classUid = classUid,
                            personUid = accountPerson.guid,
                            role = if (redeemRequest.role == PersonRoleEnum.TEACHER) {
                              if (invite.iApprovalRequired)  EnrollmentRoleEnum.PENDING_TEACHER
                              else EnrollmentRoleEnum.TEACHER
                            } else {
                                if (invite.iApprovalRequired)  EnrollmentRoleEnum.PENDING_STUDENT
                                else
                                EnrollmentRoleEnum.STUDENT
                            },
                            inviteCode = redeemRequest.code,
                        )
                    )
                )
            }
        }
        if (!invite.iInviteMultipleAllowed){
            schoolDb.getInviteEntityDao().updateInviteStatus(invite.iGuid)
        }
        return authResponse
    }
}