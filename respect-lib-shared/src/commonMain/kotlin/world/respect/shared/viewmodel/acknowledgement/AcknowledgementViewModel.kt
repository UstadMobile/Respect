package world.respect.shared.viewmodel.acknowledgement

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import world.respect.shared.domain.onboarding.ShouldShowOnboardingUseCase
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.navigation.AssignmentList
import world.respect.shared.navigation.GetStartedScreen
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.Onboarding
import world.respect.shared.navigation.RespectAppLauncher
import world.respect.shared.viewmodel.RespectViewModel

data class AcknowledgementUiState(
    val isLoading: Boolean = false,
    val isChild: Boolean = false,
)
class AcknowledgementViewModel(
    savedStateHandle: SavedStateHandle,
    private val accountManager: RespectAccountManager,
    private val shouldShowOnboardingUseCase: ShouldShowOnboardingUseCase,
) : RespectViewModel(savedStateHandle) {
    private val _uiState = MutableStateFlow(AcknowledgementUiState())

    val uiState = _uiState.asStateFlow()

    init {

        viewModelScope.launch {
            val selectedPerson = accountManager.selectedAccountAndPersonFlow.firstOrNull()
            _uiState.update { prev ->
                prev.copy(
                    isChild = selectedPerson?.isChild == true
                )
            }
        }
        viewModelScope.launch {
            _appUiState.update { prev ->
                prev.copy(
                    hideBottomNavigation = true,
                    hideAppBar = true
                )
            }

            delay(2000)

            val hasAccount = accountManager.activeAccount != null

            _navCommandFlow.tryEmit(
                NavCommand.Navigate(
                    destination = when {
                        shouldShowOnboardingUseCase() -> Onboarding
                        hasAccount -> if (_uiState.value.isChild) AssignmentList else RespectAppLauncher()
                        else -> GetStartedScreen()
                    },
                    clearBackStack = true,
                )
            )
        }
    }
}
