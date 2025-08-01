package world.respect.shared.viewmodel.manageuser.getstarted

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import world.respect.credentials.passkey.GetCredentialUseCase
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.invalid_school_name
import world.respect.shared.generated.resources.lets_get_started
import world.respect.shared.navigation.JoinClazzWithCode
import world.respect.shared.navigation.LoginScreen
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.RespectAppLauncher
import world.respect.shared.resources.StringResourceUiText
import world.respect.shared.viewmodel.RespectViewModel


class GetStartedViewModel(
    savedStateHandle: SavedStateHandle,
    getCredentialUseCase: GetCredentialUseCase
) : RespectViewModel(savedStateHandle) {

    private val _uiState = MutableStateFlow(GetStartedUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _appUiState.update { prev ->
                prev.copy(
                    title = getString(Res.string.lets_get_started),
                    hideBottomNavigation = true,
                    userAccountIconVisible = false,
                    showBackButton = false
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

    fun onSchoolNameChanged(name: String) {
        _uiState.update {
            it.copy(
                schoolName = name,
                errorMessage = null
            )
        }
    }

    fun onClickAddMySchool() {
        val name = uiState.value.schoolName
        if (name.isBlank()) {
            _uiState.update {
                it.copy(errorMessage = StringResourceUiText(Res.string.invalid_school_name))
            }
            return
        }

        viewModelScope.launch {
            _navCommandFlow.tryEmit(
                NavCommand.Navigate(LoginScreen)
            )
        }
    }

    fun onClickIHaveCode() {
        viewModelScope.launch {
            _navCommandFlow.tryEmit(
                NavCommand.Navigate(JoinClazzWithCode)
            )
        }
    }

    fun onClickOtherOptions() {
    }

    fun onClickAlreadyHaveAccount() {
        viewModelScope.launch {
            _navCommandFlow.tryEmit(NavCommand.Navigate(LoginScreen))
        }
    }
}
data class GetStartedUiState(
    val schoolName: String = "",
    val errorText: String ? = null,
    val errorMessage: StringResourceUiText? = null,
)
