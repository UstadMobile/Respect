package world.respect.shared.viewmodel.manageuser.confirmation


import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.scope.Scope
import world.respect.credentials.passkey.RespectPasswordCredential
import world.respect.shared.domain.account.invite.RespectRedeemInviteRequest
import world.respect.datalayer.respect.model.SchoolDirectoryEntry
import world.respect.datalayer.respect.model.invite.RespectInviteInfo
import world.respect.datalayer.school.model.Invite
import world.respect.datalayer.school.model.NewUserInvite
import world.respect.datalayer.school.model.Person
import world.respect.datalayer.school.model.PersonRoleEnum
import world.respect.shared.domain.account.invite.GetInviteInfoUseCase
import world.respect.shared.domain.getdeviceinfo.GetDeviceInfoUseCase
import world.respect.shared.domain.getdeviceinfo.toUserFriendlyString
import world.respect.shared.domain.school.SchoolPrimaryKeyGenerator
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.invalid_invite_code
import world.respect.shared.generated.resources.invitation
import world.respect.shared.generated.resources.congratulation
import world.respect.shared.navigation.ConfirmationScreen
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.SignupScreen
import world.respect.shared.navigation.TermsAndCondition
import world.respect.shared.resources.StringResourceUiText
import world.respect.shared.util.di.SchoolDirectoryEntryScopeId
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.manageuser.profile.ProfileType

data class ConfirmationUiState(
    val inviteInfo: RespectInviteInfo? = null,
    val inviteInfoError: StringResourceUiText? = null,
    val isTeacherInvite: Boolean = false,
)

class ConfirmationViewModel(
    savedStateHandle: SavedStateHandle,
    private val getDeviceInfoUseCase: GetDeviceInfoUseCase,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    private val route: ConfirmationScreen = savedStateHandle.toRoute()

    override val scope: Scope
        get() = getKoin().getOrCreateScope<SchoolDirectoryEntry>(
            SchoolDirectoryEntryScopeId(route.schoolUrl, null).scopeId
        )

    private val getInviteInfoUseCase: GetInviteInfoUseCase = scope.get()

    private val schoolPrimaryKeyGenerator: SchoolPrimaryKeyGenerator = scope.get()

    private val _uiState = MutableStateFlow(ConfirmationUiState())

    val uiState = _uiState.asStateFlow()


    init {

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

        _appUiState.update {
            it.copy(
                title =if ((uiState.value.inviteInfo?.invite as? NewUserInvite)?.firstUser == true)
                    Res.string.congratulation.asUiText()
                else
                    Res.string.invitation.asUiText(),
                hideBottomNavigation = true,
                userAccountIconVisible = false
            )
        }
    }

    fun onClickStudent() {
        navigateToAppropriateScreen(ProfileType.STUDENT)
    }

    fun onClickParent() {
        navigateToAppropriateScreen(ProfileType.PARENT)
    }

    fun onClickNext() {
        navigateToAppropriateScreen(ProfileType.TEACHER)
    }

    private fun navigateToAppropriateScreen(profileType: ProfileType){
        val inviteInfo = uiState.value.inviteInfo

        if (inviteInfo==null) {
            _uiState.update {
                it.copy(inviteInfoError = StringResourceUiText(Res.string.invalid_invite_code))
            }

            return
        }

        /*
        val redeemRequest = makeBlankRedeemInviteRequest(
            route.code, profileType, classUid = "TODO", invite = TODO()//inviteInfo.classGuid?:inviteInfo.invite?.forClassGuid,inviteInfo.invite
        )

        if (profileType == ProfileType.STUDENT) {

           gotToSignUpScreen(profileType,redeemRequest)
        }else {
            if (redeemRequest.invite.forFamilyOfGuid!=null &&redeemRequest.invite.newRole== PersonRoleEnum.STUDENT){
                gotToSignUpScreen(ProfileType.CHILD,redeemRequest)
                return
            }
            _navCommandFlow.tryEmit(
                NavCommand.Navigate(
                    TermsAndCondition.create(route.schoolUrl, profileType,redeemRequest,route.inviteType
                    )
                )
            )
        }
         */
    }
    fun gotToSignUpScreen(profileType: ProfileType, redeemRequest: RespectRedeemInviteRequest) {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                SignupScreen.create(
                    route.schoolUrl, profileType,redeemRequest
                )
            )
        )
    }
    fun makeBlankRedeemInviteRequest(
        inviteCode: String,
        profileType: ProfileType,
        classUid: String?,
        invite: Invite?,
    ): RespectRedeemInviteRequest {
        val role = when (profileType) {
                ProfileType.STUDENT -> PersonRoleEnum.STUDENT
                ProfileType.PARENT -> PersonRoleEnum.PARENT
                ProfileType.TEACHER -> invite?.newRole ?: PersonRoleEnum.STUDENT
                else -> throw IllegalArgumentException("Cannot use CHILD here")
            }


        val blankAccount = RespectRedeemInviteRequest.Account(
            guid = schoolPrimaryKeyGenerator.primaryKeyGenerator.nextId(Person.TABLE_ID).toString(),
            username = "",
            credential = RespectPasswordCredential(username = "", password = ""),
        )
        if (invite==null) throw IllegalStateException("invite is null")
        return RespectRedeemInviteRequest(
            code = inviteCode,
            classUid = classUid,
            role = role,
            accountPersonInfo = RespectRedeemInviteRequest.PersonInfo(),
            parentOrGuardianRole = null,
            account = blankAccount,
            deviceName = getDeviceInfoUseCase().toUserFriendlyString(),
            deviceInfo = getDeviceInfoUseCase(),
            invite = invite
        )
    }
}
