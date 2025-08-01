package world.respect.shared.viewmodel.manageuser.signup

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.getString
import world.respect.credentials.passkey.CreatePasskeyUseCase
import world.respect.datalayer.oneroster.rostering.model.OneRosterGenderEnum
import world.respect.datalayer.respect.model.invite.RespectRedeemInviteRequest
import world.respect.shared.domain.account.invite.SubmitRedeemInviteRequestUseCase
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.create_account
import world.respect.shared.generated.resources.username_required
import world.respect.shared.navigation.CreateAccount
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.SignupScreen
import world.respect.shared.navigation.WaitingForApproval
import world.respect.shared.resources.StringResourceUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.manageuser.profile.ProfileType

data class CreateAccountViewModelUiState(
    val username: String = "",
    val usernameError: StringResourceUiText? = null,
    val generalError: StringResourceUiText? = null,
    val passkeyError: String? = null
)

class CreateAccountViewModel(
    savedStateHandle: SavedStateHandle,
    private val submitRedeemInviteRequestUseCase: SubmitRedeemInviteRequestUseCase,
    private val createPasskeyUseCase: CreatePasskeyUseCase
) : RespectViewModel(savedStateHandle) {
    private val route: CreateAccount = savedStateHandle.toRoute()

    private val _uiState = MutableStateFlow(CreateAccountViewModelUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _appUiState.update {
                it.copy(
                    title = getString(Res.string.create_account),
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
                val account = RespectRedeemInviteRequest.Account(
                    username = username,
                    credential = "credential"
                )
                val redeemRequest = RespectRedeemInviteRequest(
                    inviteInfo = route.inviteInfo,
                    student = RespectRedeemInviteRequest.PersonInfo(
                        name = "Student",
                        gender = OneRosterGenderEnum.MALE,
                        dateOfBirth = LocalDate.parse("2010-01-01")
                    ),
                    parentOrGuardian = RespectRedeemInviteRequest.PersonInfo(
                        name = "Parent",
                        gender = OneRosterGenderEnum.FEMALE,
                        dateOfBirth = LocalDate.parse("1980-05-05")
                    ),
                    parentOrGuardianRole = RespectRedeemInviteRequest.GuardianRole.MOTHER,
                    account = account
                )

                val result = submitRedeemInviteRequestUseCase(redeemRequest)

                val createPasskeyResult = createPasskeyUseCase(
                    username = username,
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

    fun onOtherOptionsClick() {
    }
}
