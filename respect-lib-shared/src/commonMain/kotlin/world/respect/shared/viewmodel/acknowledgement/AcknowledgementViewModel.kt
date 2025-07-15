package world.respect.shared.viewmodel.acknowledgement

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.RespectAppLauncher
import world.respect.shared.viewmodel.RespectViewModel

data class AcknowledgementUiState(
    val isLoading: Boolean = false,
)
class AcknowledgementViewModel(
    savedStateHandle: SavedStateHandle
) : RespectViewModel(savedStateHandle) {
    private val _uiState = MutableStateFlow(AcknowledgementUiState())
    val uiState = _uiState.asStateFlow()

    init {

        viewModelScope.launch {
            _appUiState.update { prev ->
                prev.copy(
                    hideBottomNavigation = true,
                    hideAppBar = true
                )
            }
            delay(2000)
            _navCommandFlow.tryEmit(
                NavCommand.NavigateAndClearBackStack(RespectAppLauncher)
            )
        }
    }
}
