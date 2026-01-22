package world.respect.shared.viewmodel.manageuser.sharefeedback

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.launchers.WebLauncher
import world.respect.shared.domain.launchers.WhatsAppLauncher
import world.respect.shared.viewmodel.RespectViewModel

data class ShareFeedbackUiState(
    val selectedAccount: String? = null
)

class ShareFeedbackViewModel(
    private val respectAccountManager: RespectAccountManager,
    savedStateHandle: SavedStateHandle,
    private val whatsAppLauncher: WhatsAppLauncher,
    private val webLauncher: WebLauncher
) : RespectViewModel(savedStateHandle) {

    private val _uiState = MutableStateFlow(ShareFeedbackUiState())

    val uiState = _uiState.asStateFlow()


    fun onClickWhatsApp() {
        viewModelScope.launch {
            whatsAppLauncher.launchWhatsApp()
        }
    }

    fun onClickEmail() {
        // Open email
    }

    fun onClickPublicForum() {
        viewModelScope.launch {
            webLauncher.launchWeb()
        }
    }
}
