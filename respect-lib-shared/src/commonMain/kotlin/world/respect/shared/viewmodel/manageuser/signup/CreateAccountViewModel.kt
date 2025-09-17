package world.respect.shared.viewmodel.manageuser.signup

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import world.respect.credentials.passkey.CreatePasskeyUseCase
import world.respect.shared.domain.account.createinviteredeemrequest.RespectRedeemInviteRequestUseCase
import world.respect.shared.domain.account.invite.RedeemInviteUseCase
import world.respect.shared.domain.account.signup.SignupCredential
import world.respect.shared.domain.account.signup.SignupUseCase
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.app_name
import world.respect.shared.generated.resources.create_account
import world.respect.shared.generated.resources.username_required
import world.respect.shared.navigation.CreateAccount
import world.respect.shared.navigation.HowPasskeyWorks
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.OtherOptionsSignup
import world.respect.shared.navigation.SignupScreen
import world.respect.shared.navigation.WaitingForApproval
import world.respect.shared.resources.StringResourceUiText
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.manageuser.profile.ProfileType

data class CreateAccountViewModelUiState(
    val username: String = "",
    val usernameError: StringResourceUiText? = null,
    val generalError: StringResourceUiText? = null,
    val signupError: String? = null
)

class CreateAccountViewModel(
    savedStateHandle: SavedStateHandle,
    private val submitRedeemInviteRequestUseCase: RedeemInviteUseCase,
    private val createPasskeyUseCase: CreatePasskeyUseCase,
    private val respectRedeemInviteRequestUseCase: RespectRedeemInviteRequestUseCase,
    private val signupUseCase: SignupUseCase
) : RespectViewModel(savedStateHandle) {
    private val route: CreateAccount = savedStateHandle.toRoute()

    private val _uiState = MutableStateFlow(CreateAccountViewModelUiState())

    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _appUiState.update {
                it.copy(
                    title = Res.string.create_account.asUiText(),
                    hideBottomNavigation = true,
                    userAccountIconVisible = false
                )
            }
        }
    }

    fun onUsernameChanged(newValue: String) {
        _uiState.update {
            it.copy(
                username = newValue,
                usernameError = null,
                generalError = null
            )
        }
    }

    fun onClickSignupWithPasskey() {
        viewModelScope.launch {
            val username = _uiState.value.username

            _uiState.update {
                it.copy(
                    usernameError = if (username.isBlank()) StringResourceUiText(Res.string.username_required) else null
                )
            }

            if (username.isBlank()) return@launch

            try {

                val redeemRequest = respectRedeemInviteRequestUseCase(route.inviteInfo,username)

                val result = submitRedeemInviteRequestUseCase(redeemRequest)

                val createPasskeyResult = createPasskeyUseCase(
                    username = username,
                    appName = getString(Res.string.app_name)
                )
                when (createPasskeyResult) {
                    is CreatePasskeyUseCase.PasskeyCreatedResult -> {
                        val signupCredential = SignupCredential.Passkey(
                            username = username,
                            authenticationResponseJSON = createPasskeyResult.authenticationResponseJSON
                        )
                        sendSignupCredential(signupCredential)
                        when (route.type) {
                            ProfileType.CHILD , ProfileType.STUDENT->{
                                viewModelScope.launch {
                                    _navCommandFlow.tryEmit(
                                        NavCommand.Navigate(
                                            WaitingForApproval.create(
                                                route.type,
                                                route.inviteInfo,
                                                result.person.guid
                                            )
                                        )
                                    )
                                }
                            }

                            ProfileType.PARENT ->{
                                viewModelScope.launch {
                                    _navCommandFlow.tryEmit(
                                        NavCommand.Navigate(SignupScreen.create(ProfileType.CHILD,route.inviteInfo))
                                    )
                                }
                            }
                        }
                    }

                    is CreatePasskeyUseCase.Error -> {
                        _uiState.update { prev ->
                            prev.copy(
                                signupError = createPasskeyResult.message,
                            )
                        }
                    }

                    is CreatePasskeyUseCase.UserCanceledResult -> {
                        // do nothing
                    }
                }

            } catch (e: Exception) {
              println(e.message.toString())
            }
        }
    }
    private fun sendSignupCredential(credential: SignupCredential) {
        viewModelScope.launch {
            try {
                signupUseCase(credential)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(signupError = "${e.message}")
                }
            }
        }
    }
    fun onClickHowPasskeysWork() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(HowPasskeyWorks)
        )
    }
    fun onOtherOptionsClick() {
        val username = _uiState.value.username

        _uiState.update {
            it.copy(
                usernameError = if (username.isBlank()) StringResourceUiText(Res.string.username_required) else null
            )
        }

        if (username.isBlank()) return

        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                OtherOptionsSignup.create(
                    username = uiState.value.username,
                    profileType = route.type,
                    inviteInfo = route.inviteInfo
                )
            )
        )
    }

}
