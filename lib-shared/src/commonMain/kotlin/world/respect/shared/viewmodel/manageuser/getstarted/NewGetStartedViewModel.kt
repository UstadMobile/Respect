package world.respect.shared.viewmodel.manageuser.getstarted

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import world.respect.shared.domain.navigation.onappstart.NavigateOnAppStartUseCase
import world.respect.shared.domain.onboarding.ShouldShowOnboardingUseCase
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.lets_get_started
import world.respect.shared.navigation.GetStartedScreen
import world.respect.shared.navigation.NavCommand
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel

class NewGetStartedViewModel(
    savedStateHandle: SavedStateHandle,
    private val settings: Settings,
    private val navigateOnAppStartUseCase: NavigateOnAppStartUseCase,
) : RespectViewModel(savedStateHandle) {
    init {
        _appUiState.update { prev ->
            prev.copy(
                title = Res.string.lets_get_started.asUiText(),
                hideBottomNavigation = true,
                userAccountIconVisible = false,
            )
        }
    }

    fun onClickConnectExistingLearningSpace() {
        settings.putString(ShouldShowOnboardingUseCase.KEY_ONBOARDING_SHOWN, true.toString())
        viewModelScope.launch {
            val command = navigateOnAppStartUseCase()
            val destination = (command as? NavCommand.Navigate)?.destination
            _navCommandFlow.tryEmit(
                if (destination is GetStartedScreen) {
                    NavCommand.Navigate(destination.copy(canGoBack = true))
                } else {
                    command
                }
            )
        }
    }
}