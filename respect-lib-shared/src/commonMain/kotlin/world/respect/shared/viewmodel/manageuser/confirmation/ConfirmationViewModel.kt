package world.respect.shared.viewmodel.manageuser.confirmation


import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import world.respect.datalayer.respect.model.invite.RespectInviteInfo
import world.respect.shared.domain.account.invite.GetInviteInfoUseCase
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.invalid_invite_code
import world.respect.shared.generated.resources.invitation
import world.respect.shared.navigation.ConfirmationScreen
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.TermsAndCondition
import world.respect.shared.resources.StringResourceUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.manageuser.profile.ProfileType

data class ConfirmationUiState(
    val inviteInfo: RespectInviteInfo? = null,
    val inviteInfoError: StringResourceUiText? = null,
    val isTeacherInvite: Boolean = false
)

class ConfirmationViewModel(
    savedStateHandle: SavedStateHandle,
    private val getInviteInfoUseCase: GetInviteInfoUseCase
) : RespectViewModel(savedStateHandle) {

    private val _uiState = MutableStateFlow(ConfirmationUiState())
    val uiState = _uiState.asStateFlow()

    private val route: ConfirmationScreen = savedStateHandle.toRoute()

    init {
        viewModelScope.launch {
            _appUiState.update {
                it.copy(
                    title = getString(Res.string.invitation),
                    hideBottomNavigation = true,
                    userAccountIconVisible = false
                )
            }

            val inviteInfo = getInviteInfoUseCase.invoke(route.code)
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
        navigateToTermsAndCondition(ProfileType.STUDENT)
    }

    fun onClickParent() {
        navigateToTermsAndCondition(ProfileType.PARENT)
    }

    private fun navigateToTermsAndCondition(profileType: ProfileType){
        viewModelScope.launch {
            val inviteInfo= uiState.value.inviteInfo
            if (inviteInfo==null) {
                _uiState.update {
                    it.copy(inviteInfoError = StringResourceUiText(Res.string.invalid_invite_code))
                }
                return@launch
            }
            _navCommandFlow.tryEmit(
                NavCommand.Navigate(TermsAndCondition.create(profileType,inviteInfo))
            )
        }
    }

    fun onClickNext() {
    }
}
