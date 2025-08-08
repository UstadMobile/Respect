package world.respect.shared.viewmodel.manageuser.otheroptionsignup

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
import world.respect.shared.domain.account.invite.SubmitRedeemInviteRequestUseCase
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.other_options
import world.respect.shared.navigation.EnterPasswordSignup
import world.respect.shared.navigation.HowPasskeyWorks
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.OtherOptionsSignup
import world.respect.shared.navigation.SignupScreen
import world.respect.shared.navigation.WaitingForApproval
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.manageuser.profile.ProfileType

class OtherOptionsSignupViewModel(
    savedStateHandle: SavedStateHandle,
    private val createPasskeyUseCase: CreatePasskeyUseCase,
    private val submitRedeemInviteRequestUseCase: SubmitRedeemInviteRequestUseCase,
    private val respectRedeemInviteRequestUseCase: RespectRedeemInviteRequestUseCase
) : RespectViewModel(savedStateHandle) {
    private val route: OtherOptionsSignup = savedStateHandle.toRoute()

    private val _uiState = MutableStateFlow(OtherOptionsSignupUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _appUiState.update {
                it.copy(
                    title = getString(Res.string.other_options),
                    hideBottomNavigation = true,
                    userAccountIconVisible = false
                )
            }
        }
    }


    fun onClickSignupWithPasskey() {
        viewModelScope.launch {

            try {

                val redeemRequest = respectRedeemInviteRequestUseCase(route.inviteInfo,route.username)

                val result = submitRedeemInviteRequestUseCase(redeemRequest)

                val createPasskeyResult = createPasskeyUseCase(
                    username = route.username,
                )
                when (createPasskeyResult) {
                    is CreatePasskeyUseCase.PasskeyCreatedResult -> {
                        when (route.type) {
                            ProfileType.CHILD , ProfileType.STUDENT->{
                                viewModelScope.launch {
                                    _navCommandFlow.tryEmit(
                                        NavCommand.Navigate(WaitingForApproval.create(route.type,route.inviteInfo,result.uid))
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
                                passkeyError = createPasskeyResult.message,
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

    fun onClickSignupWithPassword() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(EnterPasswordSignup.create(route.username,route.type,route.inviteInfo))
        )
    }

    fun onClickHowPasskeysWork() {
        _navCommandFlow.tryEmit(NavCommand.Navigate(HowPasskeyWorks))
    }
}
data class OtherOptionsSignupUiState(
    val passkeyError: String? = null
)
