package world.respect.shared.viewmodel.manageuser.sharefeedback

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.scope.Scope
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.launchers.WebLauncherUseCase
import world.respect.shared.domain.launchers.WhatsAppLauncherUseCase
import world.respect.shared.domain.sharelink.LaunchSendEmailUseCase
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.send_feedback
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel

class ShareFeedbackViewModel(
    accountManager: RespectAccountManager,
    savedStateHandle: SavedStateHandle,
    private val whatsAppLauncherUseCase: WhatsAppLauncherUseCase,
    private val emailLauncherUseCase: LaunchSendEmailUseCase,
    private val webLauncherUseCase: WebLauncherUseCase,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()

    init {
        _appUiState.update {
            it.copy(
                title = Res.string.send_feedback.asUiText(),
                hideBottomNavigation = true,
                userAccountIconVisible = false
            )
        }
    }

    fun onClickWhatsApp() {
        viewModelScope.launch {
            whatsAppLauncherUseCase.launchWhatsApp(WHATSAPP_NUMBER)
        }
    }

    fun onClickEmail() {
        viewModelScope.launch {
            emailLauncherUseCase.invoke(
                subject = EMAIL_SUBJECT,
                body = "",
                emailId = EMAIL_ADDRESS
            )
        }
    }

    fun onClickPublicForum() {
        viewModelScope.launch {
            webLauncherUseCase.launchWeb(FORUM_URL)
        }
    }

    companion object {
        const val EMAIL_ADDRESS = "info@ustadmobile.com"
        const val EMAIL_SUBJECT = "RESPECT Feedback"
        const val WHATSAPP_NUMBER = "+1234567890" // TODO: replace with actual number
        const val FORUM_URL = "https://github.com/UstadMobile/Respect/discussions"
    }
}
