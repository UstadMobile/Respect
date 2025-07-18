package world.respect.shared.viewmodel.manageuser.waitingforapproval

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.waiting_title
import world.respect.shared.viewmodel.RespectViewModel


data class WaitingForApprovalUiState(
    val className: String = "",
    val isRefreshing: Boolean = false
)

class WaitingForApprovalViewModel(
    savedStateHandle: SavedStateHandle
) : RespectViewModel(savedStateHandle) {

    private val _uiState = MutableStateFlow(WaitingForApprovalUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _appUiState.update {
                it.copy(
                    title = getString(Res.string.waiting_title),
                    hideBottomNavigation = true,
                    userAccountIconVisible = true
                )
            }

            val className = savedStateHandle["className"] ?: "Class"
            _uiState.update { it.copy(className = className) }
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
