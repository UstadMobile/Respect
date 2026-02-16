package world.respect.shared.domain.account.invite

import com.russhwolf.settings.Settings
import io.ktor.http.Url
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.util.ext.isSameAccount

class EnableSharedDeviceModeUseCase(
    private val accountManager: RespectAccountManager,
    private val settings: Settings,
) {
    suspend operator fun invoke(
        redeemInviteRequest: RespectRedeemInviteRequest,
        schoolUrl: Url,
        isActiveUserIsTeacherOrAdmin: Boolean = false
    ) {
        try {
            accountManager.register(
                redeemInviteRequest = redeemInviteRequest,
                schoolUrl = schoolUrl,
                isActiveUserIsTeacherOrAdmin = isActiveUserIsTeacherOrAdmin
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