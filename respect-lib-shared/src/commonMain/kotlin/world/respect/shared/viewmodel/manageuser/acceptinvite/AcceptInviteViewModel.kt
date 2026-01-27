package world.respect.shared.viewmodel.manageuser.acceptinvite


import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import io.ktor.http.Url
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.scope.Scope
import world.respect.credentials.passkey.RespectPasswordCredential
import world.respect.datalayer.RespectAppDataSource
import world.respect.datalayer.ext.dataOrNull
import world.respect.datalayer.respect.model.SchoolDirectoryEntry
import world.respect.datalayer.respect.model.invite.RespectInviteInfo
import world.respect.datalayer.school.ext.isChildUser
import world.respect.datalayer.school.model.Person
import world.respect.lib.opds.model.LangMap
import world.respect.shared.domain.account.invite.GetInviteInfoUseCase
import world.respect.shared.domain.account.invite.RespectRedeemInviteRequest
import world.respect.shared.domain.account.invite.RespectRedeemInviteRequest.PersonInfo
import world.respect.shared.domain.getdeviceinfo.GetDeviceInfoUseCase
import world.respect.shared.domain.getdeviceinfo.toUserFriendlyString
import world.respect.shared.domain.school.SchoolPrimaryKeyGenerator
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.invitation
import world.respect.shared.navigation.AcceptInvite
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.SignupScreen
import world.respect.shared.navigation.TermsAndCondition
import world.respect.shared.resources.UiText
import world.respect.shared.util.di.SchoolDirectoryEntryScopeId
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.manageuser.profile.ProfileType

data class AcceptInviteUiState(
    val inviteInfo: RespectInviteInfo? = null,
    val errorText: UiText? = null,
    val isTeacherInvite: Boolean = false,
    val schoolName: LangMap? = null,
    val schoolUrl: Url? = null,
) {
    val nextButtonEnabled: Boolean
        get() = inviteInfo?.invite != null

}

class AcceptInviteViewModel(
    savedStateHandle: SavedStateHandle,
    private val getDeviceInfoUseCase: GetDeviceInfoUseCase,
    private val respectAppDataSource: RespectAppDataSource,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    private val route: AcceptInvite = savedStateHandle.toRoute()

    override val scope: Scope
        get() = getKoin().getOrCreateScope<SchoolDirectoryEntry>(
            SchoolDirectoryEntryScopeId(route.schoolUrl, null).scopeId
        )

    private val getInviteInfoUseCase: GetInviteInfoUseCase = scope.get()

    private val schoolPrimaryKeyGenerator: SchoolPrimaryKeyGenerator = scope.get()

    private val _uiState = MutableStateFlow(
        AcceptInviteUiState(schoolUrl = route.schoolUrl)
    )

    val uiState = _uiState.asStateFlow()

    init {
        _appUiState.update {
            it.copy(
                title = Res.string.invitation.asUiText(),
                hideBottomNavigation = true,
                userAccountIconVisible = false,
                showBackButton = route.canGoBack,
            )
        }

        launchWithLoadingIndicator(
            onShowError = { errText ->
                _uiState.update { it.copy(errorText = errText) }
            }
        ) {
            val inviteInfo = getInviteInfoUseCase(route.code)

            _uiState.update {
                it.copy(
                    inviteInfo = inviteInfo,
                    isTeacherInvite = false
                )
            }
        }

        viewModelScope.launch {
            val schoolDirEntry = respectAppDataSource.schoolDirectoryEntryDataSource.getSchoolDirectoryEntryByUrl(
                route.schoolUrl
            ).dataOrNull() ?: return@launch

            _uiState.update {
                it.copy(schoolName = schoolDirEntry.name)
            }
        }
    }

    fun onClickNext() {
        val invite = uiState.value.inviteInfo?.invite ?: return

        val inviteRedeemRequest = RespectRedeemInviteRequest(
            code = invite.code,
            accountPersonInfo = PersonInfo(),
            account = RespectRedeemInviteRequest.Account(
                guid = schoolPrimaryKeyGenerator.primaryKeyGenerator.nextId(Person.TABLE_ID).toString(),
                username = "",
                credential = RespectPasswordCredential(username = "", password = ""),
            ),
            deviceName = getDeviceInfoUseCase().toUserFriendlyString(),
            deviceInfo = getDeviceInfoUseCase(),
            invite = invite
        )

        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                destination = if(invite.isChildUser()) {
                    TermsAndCondition.create(
                        schoolUrl = route.schoolUrl,
                        profileType = ProfileType.TEACHER,
                        inviteRequest = inviteRedeemRequest,
                        type = null,
                    )
                }else {
                    SignupScreen.create(
                        schoolUrl = route.schoolUrl,
                        profileType = ProfileType.STUDENT,
                        inviteRequest = inviteRedeemRequest,
                        type = null,
                    )
                }
            )
        )
    }

}
