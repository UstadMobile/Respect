package world.respect.shared.viewmodel.manageuser.sharefeedback

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.launchers.WebLauncher
import world.respect.shared.domain.launchers.WhatsAppLauncher
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.share_feedback
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel

data class ShareFeedbackUiState(
    val categories: List<String> = emptyList(),
    val selectedCategory: String = "",
    val feedbackText: String = "",
    val isCheckBoxSelected: Boolean = false
)

class ShareFeedbackViewModel(
    private val respectAccountManager: RespectAccountManager,
    savedStateHandle: SavedStateHandle,
    private val whatsAppLauncher: WhatsAppLauncher,
    private val webLauncher: WebLauncher
) : RespectViewModel(savedStateHandle) {

    private val _uiState = MutableStateFlow(ShareFeedbackUiState())

    val uiState = _uiState.asStateFlow()

    init {
        _appUiState.update {
            it.copy(
                title = Res.string.share_feedback.asUiText(),
            )
        }
        // Initialize the categories list
        val categoryList = listOf(
            "Respect launcher related issues",
            "Integrated Apps related issues",
            "Question",
            "Rate us",
            "Other"
        )

        _uiState.update { currentState ->
            currentState.copy(
                categories = categoryList,
                selectedCategory = categoryList.first()
            )
        }
    }

    fun onFeedbackTextChanged(text: String) {
        _uiState.update { it.copy(feedbackText = text) }
    }

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

    fun onCategorySelected(category: String) {
        _uiState.value = _uiState.value.copy(
            selectedCategory = category
        )
    }

    fun onClickCheckBox() {
        _uiState.update { prev ->
            prev.copy(
                isCheckBoxSelected = !prev.isCheckBoxSelected
            )
        }

    }

    fun onClickSubmit() {
        //submit feedback click
    }

}