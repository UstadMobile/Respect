package world.respect.shared.viewmodel.manageuser.confirmation


import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import world.respect.credentials.passkey.RespectRedeemInviteRequest
import world.respect.datalayer.respect.model.invite.RespectInviteInfo
import world.respect.datalayer.school.model.PersonRoleEnum
import world.respect.shared.domain.account.invite.GetInviteInfoUseCase
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.invalid_invite_code
import world.respect.shared.generated.resources.invitation
import world.respect.shared.navigation.ConfirmationScreen
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.SignupScreen
import world.respect.shared.navigation.TermsAndCondition
import world.respect.shared.resources.StringResourceUiText
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.manageuser.profile.ProfileType

data class ConfirmationUiState(
    val inviteInfo: RespectInviteInfo? = null,
    val inviteInfoError: StringResourceUiText? = null,
    val isTeacherInvite: Boolean = false
)

class ConfirmationViewModel(
    savedStateHandle: SavedStateHandle,
    private val getInviteInfoUseCase: GetInviteInfoUseCase,
) : RespectViewModel(savedStateHandle) {

    private val _uiState = MutableStateFlow(ConfirmationUiState())
    val uiState = _uiState.asStateFlow()

    private val route: ConfirmationScreen = savedStateHandle.toRoute()

    init {
        _appUiState.update {
            it.copy(
                title = Res.string.invitation.asUiText(),
                hideBottomNavigation = true,
                userAccountIconVisible = false
            )
        }

        viewModelScope.launch {
            val inviteInfo = getInviteInfoUseCase(route.code)

            try {
                _uiState.update {
                    it.copy(
                        inviteInfo = inviteInfo,
                        isTeacherInvite = inviteInfo.userInviteType == RespectInviteInfo.UserInviteType.TEACHER
                    )
                }
            } catch (e: Exception) {
                println(e)
            }
        }
    }

    fun onClickStudent() {
        navigateToAppropriateScreen(ProfileType.STUDENT)
    }

    fun onClickParent() {
        navigateToAppropriateScreen(ProfileType.PARENT)
    }

    private fun navigateToAppropriateScreen(profileType: ProfileType){
        viewModelScope.launch {
            val inviteInfo= uiState.value.inviteInfo
            if (inviteInfo==null) {
                _uiState.update {
                    it.copy(inviteInfoError = StringResourceUiText(Res.string.invalid_invite_code))
                }

                return@launch
            }
            val redeemRequest = makeBlankRedeemInviteRequest(route.code, profileType)
            if (profileType==ProfileType.STUDENT) {
                _navCommandFlow.tryEmit(
                    NavCommand.Navigate(SignupScreen.create(profileType,redeemRequest))
                )
            }
            else if (profileType==ProfileType.PARENT) {
                _navCommandFlow.tryEmit(
                    NavCommand.Navigate(TermsAndCondition.create(profileType,redeemRequest))
                )
            }
        }
    }
    fun makeBlankRedeemInviteRequest(inviteCode: String, profileType: ProfileType): RespectRedeemInviteRequest {
        val role = when(profileType) {
            ProfileType.STUDENT -> PersonRoleEnum.STUDENT
            ProfileType.PARENT  -> PersonRoleEnum.PARENT
            else -> PersonRoleEnum.STUDENT
        }

        val person = RespectRedeemInviteRequest.PersonInfo()

        val blankAccount = RespectRedeemInviteRequest.Account(
            username = "",
            credential = RespectRedeemInviteRequest.RedeemInvitePasswordCredential(password = "")
        )

        return RespectRedeemInviteRequest(
            code = inviteCode,
            classUid = null,
            role = role,
            accountPersonInfo = person,
            parentOrGuardianRole = null,
            account = blankAccount
        )
    }

    fun onClickNext() {
    }
}
