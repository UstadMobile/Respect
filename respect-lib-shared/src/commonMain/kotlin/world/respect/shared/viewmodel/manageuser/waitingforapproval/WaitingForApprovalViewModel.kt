package world.respect.shared.viewmodel.manageuser.waitingforapproval

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import world.respect.shared.domain.account.invite.GetInviteInfoUseCase
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.waiting_title
import world.respect.shared.navigation.WaitingForApproval
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel


data class WaitingForApprovalUiState(
    val className: String = "",
    val isRefreshing: Boolean = false
)

class WaitingForApprovalViewModel(
    savedStateHandle: SavedStateHandle,
    inviteInfoUseCase: GetInviteInfoUseCase
) : RespectViewModel(savedStateHandle) {

    private val _uiState = MutableStateFlow(WaitingForApprovalUiState())
    val uiState = _uiState.asStateFlow()
    private val route: WaitingForApproval = savedStateHandle.toRoute()

    init {
        viewModelScope.launch {
            val inviteInfo = inviteInfoUseCase(route.code)

            _appUiState.update {
                it.copy(
                    title = Res.string.waiting_title.asUiText(),
                    hideBottomNavigation = true,
                    userAccountIconVisible = true
                )
            }

            _uiState.update { it.copy(className = inviteInfo.className?:"") }
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
