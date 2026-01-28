package world.respect.shared.domain.account.invite

import io.ktor.http.Url
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import world.respect.credentials.passkey.CreatePasskeyUseCase
import world.respect.credentials.passkey.RespectPasskeyCredential
import world.respect.credentials.passkey.RespectPasswordCredential
import world.respect.credentials.passkey.RespectQRBadgeCredential
import world.respect.credentials.passkey.RespectUserHandle
import world.respect.credentials.passkey.request.GetPasskeyProviderInfoUseCase
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.adapters.toEntity
import world.respect.datalayer.db.school.adapters.toModel
import world.respect.datalayer.school.adapters.toPersonPasskey
import world.respect.datalayer.school.ext.accepterPersonRole
import world.respect.datalayer.school.ext.isApprovalRequiredNow
import world.respect.datalayer.school.model.AuthToken
import world.respect.datalayer.school.model.PersonStatusEnum
import world.respect.libutil.ext.randomString
import world.respect.libutil.util.throwable.withHttpStatus
import world.respect.shared.domain.account.AuthResponse
import world.respect.shared.domain.account.authwithpassword.GetTokenAndUserProfileWithCredentialDbImpl
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
) : RedeemInviteUseCase, KoinComponent {

    override suspend fun invoke(
        redeemRequest: RespectRedeemInviteRequest
    ): AuthResponse {
        val inviteFromDb = schoolDb.getInviteEntityDao().getInviteByInviteCode(
            redeemRequest.code
        )?.toModel()
            ?: throw IllegalArgumentException("invite not found for code: ${redeemRequest.code}")
                .withHttpStatus(404)

        val accountGuid = redeemRequest.account.guid

        val approvalRequired = inviteFromDb.isApprovalRequiredNow()

        val accountPerson = redeemRequest.accountPersonInfo.toPerson(
            role = redeemRequest.invite.accepterPersonRole,
            username = redeemRequest.account.username,
            guid = accountGuid,
        ).copy(
            status = if(approvalRequired){
                PersonStatusEnum.PENDING_APPROVAL
            }else {
                PersonStatusEnum.ACTIVE
            },
        )

        val schoolDataSourceVal = schoolDataSource(
            schoolUrl = schoolUrl, AuthenticatedUserPrincipalId(accountGuid)
        )
        schoolDataSourceVal.personDataSource.updateLocal(listOf(accountPerson))

        val credential = redeemRequest.account.credential

        val authResponse = when (credential) {
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
                    ttl = GetTokenAndUserProfileWithCredentialDbImpl.TOKEN_DEFAULT_TTL,
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

            is RespectQRBadgeCredential -> {
                throw IllegalArgumentException("Using a QR code badge to redeem invite for new account not yet supported")
            }
        }

        return authResponse
    }
}