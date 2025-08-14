package world.respect.shared.viewmodel.manageuser.otheroption

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import world.respect.shared.domain.account.invite.GetInviteInfoUseCase
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.invalid_code
import world.respect.shared.generated.resources.next
import world.respect.shared.generated.resources.other_options
import world.respect.shared.navigation.ConfirmationScreen
import world.respect.shared.navigation.NavCommand
import world.respect.shared.resources.StringResourceUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.ActionBarButtonUiState

class OtherOptionsViewModel(
    savedStateHandle: SavedStateHandle,
    private val getInviteInfoUseCase: GetInviteInfoUseCase
) : RespectViewModel(savedStateHandle) {

    private val _uiState = MutableStateFlow(OtherOptionsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _appUiState.update { prev ->
                prev.copy(
                    title = getString(Res.string.other_options),
                    hideBottomNavigation = true,
                    userAccountIconVisible = false,
                    showBackButton = true
                )
            }
        }
    }

    fun onLinkChanged(link: String) {
        _uiState.update {
            it.copy(
                link = link,
                errorMessage = null
            )
        }
    }

     fun onClickNext() {
        val link = uiState.value.link
        if (link.isBlank()) {
            _uiState.update {
                it.copy(errorMessage = StringResourceUiText(Res.string.invalid_code))
            }
            return
        }

        viewModelScope.launch {
            val inviteInfo = getInviteInfoUseCase(uiState.value.link)
            _navCommandFlow.tryEmit(
                NavCommand.Navigate(
                    ConfirmationScreen.create(inviteInfo.code)
                )
            )
        }
    }
}

data class OtherOptionsUiState(
    val link: String = "",
    val errorMessage: StringResourceUiText? = null
)
