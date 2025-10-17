package world.respect.shared.viewmodel.manageuser.enterpasswordsignup

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import world.respect.credentials.passkey.RespectPasswordCredential
import world.respect.credentials.passkey.password.SavePasswordUseCase
import world.respect.datalayer.school.model.PersonRoleEnum
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.create_account
import world.respect.shared.generated.resources.required
import world.respect.shared.navigation.EnterPasswordSignup
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.SignupScreen
import world.respect.shared.navigation.WaitingForApproval
import world.respect.shared.resources.StringResourceUiText
import world.respect.shared.resources.UiText
import world.respect.shared.util.exception.getUiTextOrGeneric
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.manageuser.profile.ProfileType

data class EnterPasswordSignupUiState(
    val password: String = "",
    val passwordError: UiText? = null,
    val generalError: UiText? = null,
)

class EnterPasswordSignupViewModel(
    savedStateHandle: SavedStateHandle,
    private val accountManager: RespectAccountManager,
    private val savePasswordUseCase: SavePasswordUseCase
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
        val password = _uiState.value.password

        _uiState.update {
            it.copy(
                passwordError = if (password.isBlank())
                    StringResourceUiText(Res.string.required)
                else
                    null
            )
        }

        if (password.isBlank())
            return

        launchWithLoadingIndicator {
            val redeemRequest = route.respectRedeemInviteRequest.copy(
                account = route.respectRedeemInviteRequest.account.copy(
                    credential = RespectPasswordCredential(
                        username = route.respectRedeemInviteRequest.account.username,
                        password = password,
                    )
                )
            )

            try {
                accountManager.register(
                    redeemInviteRequest = redeemRequest,
                    schoolUrl = route.schoolUrl,
                )
                savePasswordUseCase(
                    username = route.respectRedeemInviteRequest.account.username,
                    password = password
                )

                _navCommandFlow.tryEmit(
                    NavCommand.Navigate(
                        destination = if(redeemRequest.role == PersonRoleEnum.PARENT) {
                            SignupScreen.create(
                                schoolUrl = route.schoolUrl,
                                profileType = ProfileType.CHILD,
                                inviteRequest = redeemRequest,
                            )
                        }else {
                            WaitingForApproval()
                        },
                        clearBackStack = true
                    )
                )
            }catch(e: Throwable) {
                e.printStackTrace()
                _uiState.update {
                    it.copy(
                        generalError = e.getUiTextOrGeneric()
                    )
                }
            }
        }

    }
}