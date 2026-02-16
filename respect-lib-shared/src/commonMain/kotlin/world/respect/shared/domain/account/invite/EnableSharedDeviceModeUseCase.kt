package world.respect.shared.domain.account.invite

import com.russhwolf.settings.Settings
import io.ktor.http.Url
import world.respect.credentials.passkey.RespectPasswordCredential
import world.respect.datalayer.school.model.NewUserInvite
import world.respect.datalayer.school.model.PersonRoleEnum
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.account.invite.RespectRedeemInviteRequest.PersonInfo
import world.respect.shared.domain.getdeviceinfo.GetDeviceInfoUseCase
import world.respect.shared.domain.getdeviceinfo.toUserFriendlyString
import world.respect.shared.util.ext.isSameAccount
import java.util.UUID

class EnableSharedDeviceModeUseCase(
    private val accountManager: RespectAccountManager,
    private val settings: Settings,
    private val getDeviceInfoUseCase: GetDeviceInfoUseCase,

    ) {
    suspend operator fun invoke(inviteCode: String, deviceName: String, schoolUrl: Url) {
        try {

            val invite = NewUserInvite(
                uid = UUID.randomUUID().toString(),
                code = inviteCode,
                role = PersonRoleEnum.SHARED_SCHOOL_DEVICE
            )
            // 1. Create the redeem request
            val redeemRequest = RespectRedeemInviteRequest(
                code = inviteCode,
                accountPersonInfo = PersonInfo(),
                account = RespectRedeemInviteRequest.Account(
                    guid = UUID.randomUUID().toString(),
                    username = "",
                    credential = RespectPasswordCredential(username = "", password = "")
                ),
                deviceName = getDeviceInfoUseCase().toUserFriendlyString(),
                deviceInfo = getDeviceInfoUseCase(),
                invite = invite
            )

            accountManager.register(
                redeemInviteRequest = redeemRequest,
                schoolUrl = schoolUrl
            )

            val deviceAccount = accountManager.activeAccount
                ?: throw IllegalStateException("Device account was not set as active")

            val currentAccounts = accountManager.accounts.value
            currentAccounts.forEach { account ->
                if (!account.isSameAccount(deviceAccount)) {
                    accountManager.removeAccount(account)
                }
            }

            settings.putBoolean(SETTINGS_KEY_IS_SHARED_MODE, true)

        } catch (e: Exception) {
            println("EnableSharedDeviceModeUseCase ERROR: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    companion object {
        const val SETTINGS_KEY_IS_SHARED_MODE = "is_shared_device_mode"
    }
}