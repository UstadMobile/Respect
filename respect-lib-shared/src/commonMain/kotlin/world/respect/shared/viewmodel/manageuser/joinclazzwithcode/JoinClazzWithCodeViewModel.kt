package world.respect.shared.viewmodel.manageuser.joinclazzwithcode

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import world.respect.shared.domain.account.invite.GetInviteInfoUseCase
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.enter_code_label
import world.respect.shared.generated.resources.invalid_invite_code
import world.respect.shared.navigation.ConfirmationScreen
import world.respect.shared.navigation.LoginScreen
import world.respect.shared.navigation.NavCommand
import world.respect.shared.resources.StringResourceUiText
import world.respect.shared.viewmodel.RespectViewModel

data class JoinClazzWithCodeUiState(
    val inviteCode: String = "",
    val errorMessage: StringResourceUiText? = null,
)

class JoinClazzWithCodeViewModel(
    savedStateHandle: SavedStateHandle,
    private val getInviteInfoUseCase: GetInviteInfoUseCase
) : RespectViewModel(savedStateHandle) {

    private val _uiState = MutableStateFlow(JoinClazzWithCodeUiState())

    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _appUiState.update {prev ->
                prev.copy(
                    title = getString(Res.string.enter_code_label),
                    hideBottomNavigation = true,
                    userAccountIconVisible = false,
                    showBackButton = false

                )
            }
        }
    }

    fun onCodeChanged(code: String) {
        _uiState.update {
            it.copy(
                inviteCode = code,
                errorMessage = null
            )
        }
    }

    fun onClickNext() {
        viewModelScope.launch {
            if (uiState.value.inviteCode.isBlank()) {
                _uiState.update {
                    it.copy(errorMessage = StringResourceUiText(Res.string.invalid_invite_code))
                }
                return@launch
            }
            try {
                val inviteInfo = getInviteInfoUseCase(uiState.value.inviteCode)
                _navCommandFlow.tryEmit(
                    NavCommand.Navigate(
                        ConfirmationScreen.create(inviteInfo.code)
                    )
                )
            }catch (e:Exception){
                println(e)
            }
        }
    }
}
