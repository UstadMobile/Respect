package world.respect.shared.viewmodel.manageuser.getstarted

import androidx.lifecycle.SavedStateHandle
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.update
import world.respect.shared.domain.navigation.onappstart.NavigateOnAppStartUseCase
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.lets_get_started
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.ConnectLearningSpace
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
        _navCommandFlow.tryEmit(NavCommand.Navigate(ConnectLearningSpace))

    }
}