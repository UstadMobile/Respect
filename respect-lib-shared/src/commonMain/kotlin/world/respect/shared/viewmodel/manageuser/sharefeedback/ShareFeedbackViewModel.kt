package world.respect.shared.viewmodel.manageuser.sharefeedback

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.viewmodel.RespectViewModel

data class ShareFeedbackUiState(
    val selectedAccount: String? = null
)

class ShareFeedbackViewModel(
    private val respectAccountManager: RespectAccountManager,
    savedStateHandle: SavedStateHandle
) : RespectViewModel(savedStateHandle) {

    private val _uiState = MutableStateFlow(ShareFeedbackUiState())

    val uiState = _uiState.asStateFlow()


    fun onClickWhatsApp() {
        // Open WhatsApp
    }

    fun onClickEmail() {
        // Open email
    }

    fun onClickPublicForum() {
        // Open forum
    }
}
