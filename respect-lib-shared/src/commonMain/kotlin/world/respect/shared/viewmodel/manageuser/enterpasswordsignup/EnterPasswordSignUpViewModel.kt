package world.respect.shared.viewmodel.manageuser.enterpasswordsignup

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import world.respect.credentials.passkey.RespectRedeemInviteRequest
import world.respect.shared.domain.account.createinviteredeemrequest.RespectRedeemInviteRequestUseCase
import world.respect.shared.domain.account.invite.RedeemInviteUseCase
import world.respect.shared.domain.account.invite.GetInviteInfoUseCase
import world.respect.shared.domain.account.invite.SubmitRedeemInviteRequestUseCase
import world.respect.shared.domain.account.signup.SignupCredential
import world.respect.shared.domain.account.signup.SignupUseCase
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.create_account
import world.respect.shared.generated.resources.required_field
import world.respect.shared.navigation.EnterPasswordSignup
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.SignupScreen
import world.respect.shared.navigation.WaitingForApproval
import world.respect.shared.resources.StringResourceUiText
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.manageuser.profile.ProfileType

data class EnterPasswordSignupUiState(
    val password: String = "",
    val passwordError: StringResourceUiText? = null,
    val generalError: StringResourceUiText? = null,
)

class EnterPasswordSignupViewModel(
    savedStateHandle: SavedStateHandle,
    private val submitRedeemInviteRequestUseCase: RedeemInviteUseCase,
    private val respectRedeemInviteRequestUseCase: RespectRedeemInviteRequestUseCase,
    private val signupUseCase: SignupUseCase,
    private val inviteInfoUseCase: GetInviteInfoUseCase
) : RespectViewModel(savedStateHandle) {
    private val route: EnterPasswordSignup = savedStateHandle.toRoute()

    private val _uiState = MutableStateFlow(EnterPasswordSignupUiState())
    val uiState = _uiState.asStateFlow()

    init {
        _appUiState.update {
            it.copy(
                title = Res.string.create_account.asUiText(),
                hideBottomNavigation = true,
                userAccountIconVisible = false
            )
        }
    }

    fun onPasswordChanged(newValue: String) {
        _uiState.update {
            it.copy(
                password = newValue,
                passwordError = null,
                generalError = null
            )
        }
    }

    fun onClickSignup() {
        viewModelScope.launch {
            val password = _uiState.value.password

            _uiState.update {
                it.copy(
                    passwordError = if (password.isBlank())
                        StringResourceUiText(Res.string.required_field)
                    else
                        null
                )
            }

            if (password.isBlank()) return@launch
            val signupCredential = SignupCredential.Password(
                username = route.username,
                password = password
            )
            signupUseCase(signupCredential)
            val inviteInfo = inviteInfoUseCase(route.code)

            when (route.type) {
                 ProfileType.CHILD ->{
                     //ignore not create account for child
                 }
                 ProfileType.STUDENT -> {
                    val redeemRequest = respectRedeemInviteRequestUseCase(
                        inviteInfo = inviteInfo,
                        username = route.username,
                        personInfo = route.personInfo,
                        parentOrGuardian = null,
                        credential = RespectRedeemInviteRequest.RedeemInvitePasswordCredential(
                            password
                        )
                    )
                    val result = submitRedeemInviteRequestUseCase(redeemRequest)
                    _navCommandFlow.tryEmit(
                        NavCommand.Navigate(
                             WaitingForApproval.create(
                                profileType =   route.type,
                                inviteCode = route.code,
                                pendingInviteStateUid = result?.guid ?: ""
                            )
                        )
                    )
                }

                ProfileType.PARENT -> {
                    _navCommandFlow.tryEmit(
                        NavCommand.Navigate(
                               SignupScreen.create(
                                profileType = ProfileType.CHILD,
                                inviteCode = route.code,
                                parentPersonInfoJson = route.personInfo,
                                parentUsername = route.username,
                                parentRedeemCredential =  RespectRedeemInviteRequest.RedeemInvitePasswordCredential(
                                    password
                                )
                            )
                        )
                    )
                }
            }

        }

    }
}