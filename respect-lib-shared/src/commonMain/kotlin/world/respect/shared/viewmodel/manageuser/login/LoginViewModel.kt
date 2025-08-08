package world.respect.shared.viewmodel.manageuser.login

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import world.respect.credentials.passkey.GetCredentialUseCase
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.*
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.RespectAppLauncher
import world.respect.shared.resources.StringResourceUiText
import world.respect.shared.viewmodel.RespectViewModel

data class LoginUiState(
    val userId: String = "",
    val password: String = "",
    val errorText: String ? = null,
    val userIdError: StringResourceUiText? = null,
    val passwordError: StringResourceUiText? = null,
)

class LoginViewModel(
    savedStateHandle: SavedStateHandle,
    getCredentialUseCase: GetCredentialUseCase
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
        viewModelScope.launch {

            viewModelScope.launch {
                try {
                    when (val credentialResult = getCredentialUseCase()) {
                        is GetCredentialUseCase.PasskeyCredentialResult -> {
                            viewModelScope.launch {
                                _navCommandFlow.tryEmit(
                                    NavCommand.Navigate(RespectAppLauncher)
                                )
                            }
                        }

                        is GetCredentialUseCase.PasswordCredentialResult -> {

                        }

                        is GetCredentialUseCase.Error -> {
                            _uiState.update { prev ->
                                prev.copy(
                                    errorText = (credentialResult.message),
                                )
                            }
                            println ( "Error occurred: ${credentialResult.message}")
                        }

                        is GetCredentialUseCase.NoCredentialAvailableResult,
                        is GetCredentialUseCase.UserCanceledResult-> {
                            //do nothing
                        }

                    }
                } catch (e: Exception) {
                   println( "Error occurred: ${e.message}")
                }
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
                    userIdError = if (userID.isEmpty()) StringResourceUiText(Res.string.userid_required) else null,
                    passwordError = if (password.isEmpty()) StringResourceUiText(Res.string.password_required) else null
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
