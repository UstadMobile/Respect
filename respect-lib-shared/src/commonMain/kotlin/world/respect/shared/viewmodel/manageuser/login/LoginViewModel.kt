package world.respect.shared.viewmodel.manageuser.login

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import world.respect.credentials.passkey.GetCredentialUseCase
import world.respect.credentials.passkey.VerifyDomainUseCase
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.RespectAppDataSource
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.login
import world.respect.shared.generated.resources.required_field
import world.respect.shared.generated.resources.something_went_wrong
import world.respect.shared.navigation.JoinClazzWithCode
import world.respect.shared.navigation.LoginScreen
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.RespectAppLauncher
import world.respect.shared.resources.StringResourceUiText
import world.respect.shared.resources.StringUiText
import world.respect.shared.resources.UiText
import world.respect.shared.util.exception.getUiText
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val errorText: UiText? = null,
    val usernameError: StringResourceUiText? = null,
    val passwordError: StringResourceUiText? = null,
)

class LoginViewModel(
    savedStateHandle: SavedStateHandle,
    private val accountManager: RespectAccountManager,
    getCredentialUseCase: GetCredentialUseCase,
    respectAppDataSource: RespectAppDataSource,
    private val verifyDomainUseCase: VerifyDomainUseCase
) : RespectViewModel(savedStateHandle) {

    private val _uiState = MutableStateFlow(LoginUiState())

    val uiState = _uiState.asStateFlow()

    private val route: LoginScreen = savedStateHandle.toRoute()

    init {
        viewModelScope.launch {
            _appUiState.update { prev ->
                prev.copy(
                    title = Res.string.login.asUiText(),
                    hideBottomNavigation = true,
                    userAccountIconVisible = false
                )
            }
        }
        viewModelScope.launch {
            try {
                val school = respectAppDataSource.schoolDirectoryEntryDataSource
                    .getSchoolDirectoryEntryByUrl(route.schoolUrl)
                val rpId: String? = when (school) {
                    is DataReadyState -> school.data.rpId
                    else -> null
                }

                val isRpIDVerified = verifyDomainUseCase(rpId?:"")
                if (isRpIDVerified){
                    when (val credentialResult = getCredentialUseCase(rpId?:"")) {
                        is GetCredentialUseCase.PasskeyCredentialResult -> {
                            _navCommandFlow.tryEmit(
                                NavCommand.Navigate(
                                    destination = RespectAppLauncher, clearBackStack = true
                                )
                            )
                        }

                        is GetCredentialUseCase.PasswordCredentialResult -> {
                            onUsernameChanged(credentialResult.credentialUsername)
                            onPasswordChanged(credentialResult.password)
                            onClickLogin()
                        }

                        is GetCredentialUseCase.Error -> {
                            _uiState.update { prev ->
                                prev.copy(
                                    errorText = StringUiText(credentialResult.message ?: ""),
                                )
                            }
                        }

                        is GetCredentialUseCase.NoCredentialAvailableResult,
                        is GetCredentialUseCase.UserCanceledResult -> {
                            //do nothing
                        }

                    }

                }
            } catch (e: Exception) {
                println("Error occurred: ${e.message}")
            }
        }
    }

    fun onUsernameChanged(userId: String) {
        _uiState.update {
            it.copy(
                username = userId,
                usernameError = null
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
            val username = uiState.value.username
            val password = uiState.value.password

            _uiState.update {
                it.copy(
                    usernameError = if (username.isEmpty())
                        StringResourceUiText(Res.string.required_field)
                    else
                        null,
                    passwordError = if (password.isEmpty())
                        StringResourceUiText(Res.string.required_field)
                    else
                        null
                )
            }

            if (uiState.value.usernameError!=null || uiState.value.passwordError!=null) {
                return@launch
            }

            viewModelScope.launch {
                try {
                    accountManager.login(
                        username, password, route.schoolUrl
                    )

                    _navCommandFlow.tryEmit(
                        NavCommand.Navigate(RespectAppLauncher)
                    )
                }catch(e: Exception) {
                    e.printStackTrace()
                    _uiState.update { prev ->
                        prev.copy(
                            errorText = e.getUiText() ?: StringResourceUiText(Res.string.something_went_wrong)
                        )
                    }
                }
            }
        }
    }

    fun onClickInviteCode() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(JoinClazzWithCode.create(route.schoolUrl))
        )
    }

}
