package world.respect.shared.viewmodel.manageuser.login

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.*
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.ProfileScreen
import world.respect.shared.navigation.RespectAppLauncher
import world.respect.shared.viewmodel.RespectViewModel

data class LoginUiState(
    val userId: String = "",
    val password: String = "",
    val userIdError: String? = null,
    val passwordError: String? = null,
)

class LoginViewModel(
    savedStateHandle: SavedStateHandle
) : RespectViewModel(savedStateHandle) {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _appUiState.update { prev ->
                prev.copy(
                    title = getString(Res.string.login),
                    hideBottomNavigation = true,
                    userAccountIconVisible = false
                )
            }
        }
    }

    fun onUserIdChanged(userId: String) {
        _uiState.update {
            it.copy(
                userId = userId,
                userIdError = null
            )
        }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update {
            it.copy(
                password = password,
                passwordError = null
            )
        }
    }

    fun onClickLogin() {
        viewModelScope.launch {
            val userID = uiState.value.userId
            val password = uiState.value.password

            _uiState.update {
                it.copy(
                    userIdError = if (userID.isEmpty()) getString(Res.string.userid_required) else null,
                    passwordError = if (password.isEmpty()) getString(Res.string.password_required) else null
                )
            }

            if (uiState.value.userIdError!=null || uiState.value.passwordError!=null) {
                return@launch
            }
            viewModelScope.launch {
                _navCommandFlow.tryEmit(
                    NavCommand.Navigate(RespectAppLauncher)
                )
            }
        }
    }
}
