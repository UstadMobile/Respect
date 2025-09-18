package world.respect.shared.viewmodel.manageuser.otheroptionsignup

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import world.respect.credentials.passkey.CreatePasskeyUseCase
import world.respect.credentials.passkey.RespectRedeemInviteRequest
import world.respect.shared.domain.account.invite.GetInviteInfoUseCase
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.other_options
import world.respect.shared.generated.resources.passkey_not_supported
import world.respect.shared.navigation.EnterPasswordSignup
import world.respect.shared.navigation.HowPasskeyWorks
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.OtherOptionsSignup
import world.respect.shared.navigation.SignupScreen
import world.respect.shared.resources.StringResourceUiText
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.manageuser.profile.ProfileType

data class OtherOptionsSignupUiState(
    val passkeyError: String? = null,
    val generalError: StringResourceUiText? = null
)

class OtherOptionsSignupViewModel(
    savedStateHandle: SavedStateHandle,
    private val createPasskeyUseCase: CreatePasskeyUseCase?,
    private val inviteInfoUseCase: GetInviteInfoUseCase
) : RespectViewModel(savedStateHandle) {
    private val route: OtherOptionsSignup = savedStateHandle.toRoute()

    private val _uiState = MutableStateFlow(OtherOptionsSignupUiState())
    val uiState = _uiState.asStateFlow()

    init {
        _appUiState.update {
            it.copy(
                title = Res.string.other_options.asUiText(),
                hideBottomNavigation = true,
                userAccountIconVisible = false
            )
        }
        _uiState.update { prev ->
            prev.copy(
                generalError = if (createPasskeyUseCase == null)
                    StringResourceUiText(Res.string.passkey_not_supported)
                else null
            )
        }
    }


    fun onClickSignupWithPasskey() {
        viewModelScope.launch {
            val inviteInfo = inviteInfoUseCase(route.respectRedeemInviteRequest.code)
            try {
                val rpId = inviteInfo.school.rpId
                if (createPasskeyUseCase == null || rpId==null){
                    _uiState.update {
                        it.copy(
                            generalError = StringResourceUiText(Res.string.passkey_not_supported)
                        )
                    }
                }else {
                    val createPasskeyResult = createPasskeyUseCase(
                        username = route.username,
                        rpId = rpId
                    )

                    when (createPasskeyResult) {
                        is CreatePasskeyUseCase.PasskeyCreatedResult -> {
                            val redeemInviteRequest = route.respectRedeemInviteRequest
                            val account = RespectRedeemInviteRequest.Account(
                                username = route.username,
                                credential = RespectRedeemInviteRequest.RedeemInvitePasskeyCredential(
                                    createPasskeyResult.authenticationResponseJSON
                                )
                            )

                            val updatedRedeemInviteRequest = RespectRedeemInviteRequest(
                                code = redeemInviteRequest.code,
                                classUid = redeemInviteRequest.classUid,
                                role = redeemInviteRequest.role,
                                accountPersonInfo = redeemInviteRequest.accountPersonInfo,
                                parentOrGuardianRole = redeemInviteRequest.parentOrGuardianRole,
                                account = account
                            )



                            when (route.type) {
                                ProfileType.CHILD ->{
                                    //ignore not create account for child
                                }
                                ProfileType.TEACHER -> {

                                }
                                ProfileType.STUDENT -> {

                                }

                                ProfileType.PARENT -> {
                                    _navCommandFlow.tryEmit(
                                        NavCommand.Navigate(
                                            SignupScreen.create(
                                                profileType = ProfileType.CHILD,
                                                inviteRequest = updatedRedeemInviteRequest
                                            )
                                        )
                                    )
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
                }

            } catch (e: Exception) {
                println(e.message.toString())
            }
        }
    }

    fun onClickSignupWithPassword() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                EnterPasswordSignup.create(
                    route.respectRedeemInviteRequest
                )
            )
        )
    }

    fun onClickHowPasskeysWork() {
        _navCommandFlow.tryEmit(NavCommand.Navigate(HowPasskeyWorks))
    }
}