package world.respect.shared.viewmodel.manageuser.waitingforapproval

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.waiting_title
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel


data class WaitingForApprovalUiState(
    val className: String = "",
    val isRefreshing: Boolean = false
)

class WaitingForApprovalViewModel(
    savedStateHandle: SavedStateHandle,
) : RespectViewModel(savedStateHandle) {

    private val _uiState = MutableStateFlow(WaitingForApprovalUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {

            _appUiState.update {
                it.copy(
                    title = Res.string.waiting_title.asUiText(),
                    hideBottomNavigation = true,
                    userAccountIconVisible = true,
                    showBackButton = false,
                )
            }

            _uiState.update { it.copy(className = "Wait for it") }
        }
    }

    fun onRefresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }

            delay(1000)

            val approved = false
            if (approved) {
            }

            _uiState.update { it.copy(isRefreshing = false) }
        }
    }
}
