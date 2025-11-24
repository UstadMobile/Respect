package world.respect.shared.viewmodel.acknowledgement

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import world.respect.shared.domain.onboarding.ShouldShowOnboardingUseCase
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.navigation.Acknowledgement
import world.respect.shared.navigation.ConfirmationScreen
import world.respect.shared.navigation.GetStartedScreen
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.Onboarding
import world.respect.shared.navigation.RespectAppLauncher
import world.respect.shared.viewmodel.RespectViewModel

data class AcknowledgementUiState(
    val isLoading: Boolean = false,
)
class AcknowledgementViewModel(
    savedStateHandle: SavedStateHandle,
    private val accountManager: RespectAccountManager,
    private val shouldShowOnboardingUseCase: ShouldShowOnboardingUseCase,
) : RespectViewModel(savedStateHandle) {
    private val _uiState = MutableStateFlow(AcknowledgementUiState())

    val uiState = _uiState.asStateFlow()
    private val route: Acknowledgement = savedStateHandle.toRoute()

    init {
        viewModelScope.launch {
            _appUiState.update { prev ->
                prev.copy(
                    hideBottomNavigation = true,
                    hideAppBar = true
                )
            }

            delay(2000)

            val hasAccount = accountManager.selectedAccount != null

            _navCommandFlow.tryEmit(
                NavCommand.Navigate(
                    destination = when {
                        shouldShowOnboardingUseCase() -> Onboarding
                        route.schoolUrl != null -> ConfirmationScreen.create(
                            route.schoolUrl,
                            route.inviteCode.toString()
                        )
                        hasAccount -> RespectAppLauncher()
                        else -> GetStartedScreen()
                    },
                    clearBackStack = true,
                )
            )
        }
    }
}
