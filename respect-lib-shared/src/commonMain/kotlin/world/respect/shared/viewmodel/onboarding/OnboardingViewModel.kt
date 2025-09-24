package world.respect.shared.viewmodel.onboarding

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.viewmodel.RespectViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.agree_terms_and_conditions
import world.respect.shared.navigation.GetStartedScreen
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.RespectAppLauncher

data class OnboardingUiState(
    val isLoading: Boolean = false,
    val consentGiven: Boolean = false,
    val showConsentError: Boolean = false,
    val snackBarMessage: String? = null
)

class OnboardingViewModel(
    savedStateHandle: SavedStateHandle,
    private val accountManager: RespectAccountManager
) : RespectViewModel(savedStateHandle) {

    private val _uiState = MutableStateFlow(OnboardingUiState())

    val uiState = _uiState.asStateFlow()

    var errorMessage: String = ""


    init {
        viewModelScope.launch {
            errorMessage = getString(resource = Res.string.agree_terms_and_conditions)

            _appUiState.update { prev ->
                prev.copy(
                    hideBottomNavigation = true,
                    hideAppBar = true
                )
            }
        }
    }

    fun onConsentChanged(checked: Boolean) {
        _uiState.update { it.copy(consentGiven = checked, showConsentError = false) }
    }
    fun onClickGetStartedButton() {

        val state = _uiState.value
        if (!state.consentGiven) {

            _uiState.update {
                it.copy(
                    showConsentError = true,
                    snackBarMessage = errorMessage
                )
            }
            return
        }

        val hasAccount = accountManager.selectedAccount != null

        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                destination = if (hasAccount) {
                    RespectAppLauncher
                } else {
                    GetStartedScreen
                },
                clearBackStack = hasAccount
            )
        )
    }
    fun clearSnackBar() {
        _uiState.update { it.copy(snackBarMessage = null) }
    }
}