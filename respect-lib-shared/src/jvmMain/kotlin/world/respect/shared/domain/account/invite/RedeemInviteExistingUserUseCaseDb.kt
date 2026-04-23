package world.respect.shared.domain.account.invite

import io.ktor.http.Url
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import world.respect.credentials.passkey.CreatePasskeyUseCase
import world.respect.credentials.passkey.RespectPasskeyCredential
import world.respect.credentials.passkey.RespectPasswordCredential
import world.respect.credentials.passkey.RespectQRBadgeCredential
import world.respect.credentials.passkey.RespectUserHandle
import world.respect.credentials.passkey.request.GetPasskeyProviderInfoUseCase
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.datalayer.SchoolDataSourceLocal
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.adapters.toEntity
import world.respect.datalayer.db.school.adapters.toModel
import world.respect.datalayer.db.school.adapters.toPersonEntities
import world.respect.datalayer.school.adapters.toPersonPasskey
import world.respect.datalayer.school.ext.accepterEnrollmentRole
import world.respect.datalayer.school.ext.accepterPersonRole
import world.respect.datalayer.school.ext.copyWithInviteInfo
import world.respect.datalayer.school.ext.isApprovalRequiredNow
import world.respect.datalayer.school.model.AuthToken
import world.respect.datalayer.school.model.Invite2
import world.respect.datalayer.school.model.NewUserInvite
import world.respect.datalayer.school.model.ClassInvite
import world.respect.datalayer.school.model.ClassInviteModeEnum
import world.respect.datalayer.school.model.Enrollment
import world.respect.datalayer.school.model.PersonStatusEnum
import world.respect.datalayer.school.model.StatusEnum
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
import kotlin.time.Clock

/**
 * Server-side use case that handles redeeming an invite: that is when a (new) user client signing up
 * provies both a) invite info and code and b) information about the account they want to create (
 * name, gender, username, password/passkey, etc).
 */
class RedeemInviteExistingUserUseCaseDb(
    private val schoolDb: RespectSchoolDatabase,
    private val uidNumberMapper: UidNumberMapper,
    private val schoolUrl: Url,
    private val schoolPrimaryKeyGenerator: SchoolPrimaryKeyGenerator,
    private val getTokenAndUserProfileUseCase: GetTokenAndUserProfileWithCredentialUseCase,
    private val schoolDataSource: SchoolDataSourceLocalProvider,
    private val json: Json,
    private val getPasskeyProviderInfoUseCase: GetPasskeyProviderInfoUseCase,
    private val encryptPersonPasswordUseCase: EncryptPersonPasswordUseCase,
) : RedeemInviteExistingUserUseCase, KoinComponent {

    override suspend fun invoke(
        redeemRequest: RespectRedeemInviteRequest,
        selectedChildGuid : String ?
    ) {
        val inviteFromDb = schoolDb.getInviteEntityDao().getInviteByInviteCode(
            redeemRequest.code
        )?.toModel()
            ?: throw IllegalArgumentException("invite not found for code: ${redeemRequest.code}")
                .withHttpStatus(404)

        val accountGuid = redeemRequest.account.guid
        val approvalRequired = inviteFromDb.isApprovalRequiredNow()

        val accountPerson =
            schoolDb.getPersonEntityDao().findByGuidNum(uidNumberMapper(accountGuid))
                ?.toPersonEntities()?.toModel()?.copy(
                    status = if (approvalRequired) {
                        PersonStatusEnum.PENDING_APPROVAL
                    } else {
                        PersonStatusEnum.ACTIVE
                    },
                ).let {
                    if (approvalRequired) {
                        it?.copyWithInviteInfo(invite = redeemRequest.invite)
                    } else {
                        it
                    }
                }
                ?: throw IllegalArgumentException("existing person not found for guid: $accountGuid")
                    .withHttpStatus(404)


        val schoolDataSourceVal = schoolDataSource(
            schoolUrl = schoolUrl, AuthenticatedUserPrincipalId(accountGuid)
        )
        schoolDataSourceVal.personDataSource.updateLocal(listOf(accountPerson))

        val enrollmentRole = inviteFromDb.accepterEnrollmentRole(approvalRequired)
        if (enrollmentRole != null && inviteFromDb is ClassInvite) {
            schoolDataSourceVal.enrollmentDataSource.updateLocal(
                listOf(
                    Enrollment(
                        uid = schoolPrimaryKeyGenerator.primaryKeyGenerator.nextId(
                            Enrollment.TABLE_ID
                        ).toString(),
                        classUid = inviteFromDb.classUid,
                        personUid = if (inviteFromDb.inviteMode == ClassInviteModeEnum.VIA_PARENT) selectedChildGuid?:"" else accountPerson.guid,
                        role = enrollmentRole,
                        beginDate = Clock.System.now().toLocalDateTime(
                            TimeZone.currentSystemDefault()
                        ).date
                    )
                )
            )
        }


        markFirstUserInviteAsDeleted(inviteFromDb, schoolDataSourceVal)

    }

}