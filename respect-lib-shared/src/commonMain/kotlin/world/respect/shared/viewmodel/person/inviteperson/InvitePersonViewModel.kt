package world.respect.shared.viewmodel.person.inviteperson
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import world.respect.shared.domain.sharelink.LaunchSendEmailUseCase
import world.respect.shared.domain.sharelink.LaunchShareLinkUseCase
import world.respect.shared.domain.sharelink.LaunchSendSmsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.DataLoadingState
import world.respect.datalayer.NoDataLoadedState
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.ext.dataOrNull
import world.respect.datalayer.school.domain.GetWritableRolesListUseCase
import world.respect.datalayer.school.ext.newUserInviteUid
import world.respect.datalayer.school.model.Clazz.Companion.DEFAULT_INVITE_CODE_LEN
import world.respect.datalayer.school.model.Clazz.Companion.DEFAULT_INVITE_CODE_MAX
import world.respect.datalayer.school.model.EnrollmentRoleEnum
import world.respect.datalayer.school.model.Invite
import world.respect.datalayer.school.model.Invite2
import world.respect.datalayer.school.model.PersonRoleEnum
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.clipboard.SetClipboardStringUseCase
import world.respect.shared.domain.createlink.CreateInviteLinkUseCase
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.invitation
import world.respect.shared.generated.resources.invite_person
import world.respect.shared.navigation.CopyCode
import world.respect.shared.navigation.InvitePerson
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.QrCode
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.AppBarSearchUiState
import kotlin.random.Random

data class InvitePersonUiState(
    val inviteOptions: InvitePerson.NewUserInviteOptions = InvitePerson.NewUserInviteOptions(null),
    val invite2: DataLoadState<Invite2> = DataLoadingState(),
    val approvalRequired: Boolean = false,
    val selectedRole: PersonRoleEnum? = null,
    val shareLink: String? = null,
    val classGuid: String? = null,
    val className: String? = null,
    val classRole: EnrollmentRoleEnum? = null,
    val schoolName: String? = null,
    val familyPersonGuid: String? = null,
    val roleOptions: List<PersonRoleEnum> = emptyList(),
    val invite: Invite? = null
) {
    val inviteCode: String?
        get() = invite2.dataOrNull()?.code
}

class InvitePersonViewModel(
    savedStateHandle: SavedStateHandle,
    private val accountManager: RespectAccountManager,
    private val setClipboardStringUseCase: SetClipboardStringUseCase,
    private val smsLinkLauncher: LaunchSendSmsUseCase,
    private val shareLinkLauncher: LaunchShareLinkUseCase,
    private val emailLinkLauncher: LaunchSendEmailUseCase
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    private val route: InvitePerson = savedStateHandle.toRoute()

    override val scope: Scope = accountManager.requireActiveAccountScope()

    private val createInviteLinkUseCase: CreateInviteLinkUseCase by lazy {
        scope.get()
    }

    private val schoolDataSource: SchoolDataSource by inject()

    private val _uiState = MutableStateFlow(InvitePersonUiState())

    val uiState = _uiState.asStateFlow()

    /**
     * This ViewModel is a little different to the "normal" case. When the user selects a new user
     * role from the dropdown, this changes the uid of the invite that we want to actually show
     *
     * Hence the inviteUid is modelled as a flow, which is then collected.
     */
    private val _inviteUid = MutableStateFlow<String?>(null)

    private val getWritableRolesListUseCase: GetWritableRolesListUseCase by inject()

    init {
        _appUiState.update {
            it.copy(
                title = Res.string.invite_person.asUiText(),
                searchState = AppBarSearchUiState(visible = false),
                showBackButton = true,
                hideBottomNavigation = true,
                userAccountIconVisible = false
            )
        }

        _inviteUid.update {
            when(route.invitePersonOptions) {
                is InvitePerson.NewUserInviteOptions -> {
                    route.invitePersonOptions.presetRole?.newUserInviteUid
                }

                //else -> null
            }
        }

        viewModelScope.launch {
            val currentPersonRole = accountManager.selectedAccountAndPersonFlow.first()
                ?.person?.roles?.first()?.roleEnum ?: return@launch

            val writableRoles = getWritableRolesListUseCase(currentPersonRole)

            _uiState.update {
                it.copy(
                    roleOptions =  writableRoles,
                    selectedRole = writableRoles.first()
                )
            }

            _inviteUid.collectLatest { inviteUid ->
                if(inviteUid != null) {
                    schoolDataSource.inviteDataSource.findByUidAsFlow(
                        uid = inviteUid,
                        loadParams = DataLoadParams()
                    ).collect { invite ->
                        _uiState.update { it.copy(invite2 = invite) }
                    }
                }else {
                    _uiState.update { it.copy(invite2 = NoDataLoadedState.notFound()) }
                }
            }
        }
    }

    fun copyInviteLinkToClipboard() {
        viewModelScope.launch {
          createOrEditInvite()
            _uiState.value.shareLink?.also { link ->
                setClipboardStringUseCase(link)
            }
        }
    }


    fun setApprovalRequired(enabled: Boolean) {
        _uiState.update { it.copy(approvalRequired = enabled) }
    }


    fun onClickGetCode() {
        viewModelScope.launch {
            createOrEditInvite()
            _navCommandFlow.tryEmit(
                NavCommand.Navigate(
                    CopyCode(inviteCode = uiState.value.inviteCode)
                )
            )
        }
    }

    fun onRoleChange(role: PersonRoleEnum) {
        _inviteUid.update { role.newUserInviteUid }
        _uiState.update { it.copy(selectedRole = role) }
    }

    fun onSendLinkViaSms() {
        viewModelScope.launch {
            val link =  createOrEditInvite()
            smsLinkLauncher.sendLink(link)
        }

    }

    fun onSendLinkViaEmail() {
        viewModelScope.launch {
            val link =  createOrEditInvite()
            emailLinkLauncher.sendEmail(getString(Res.string.invitation) ,link)
        }
    }

    fun onShareLink() {
        viewModelScope.launch {
            val link =  createOrEditInvite()
            shareLinkLauncher.launch(link)
        }
    }

    fun onClickQrCode(){
        viewModelScope.launch {
            createOrEditInvite()
            _navCommandFlow.tryEmit(
                NavCommand.Navigate(
                    QrCode(
                        inviteLink = uiState.value.shareLink,
                        schoolOrClass = uiState.value.className?:uiState.value.schoolName)
                )
            )
        }
    }
    private fun generateCode(): String {
        return Random.nextInt(DEFAULT_INVITE_CODE_MAX)
            .toString()
            .padStart(DEFAULT_INVITE_CODE_LEN, '0')
    }

    private suspend fun storeInvite(invite: Invite): Invite {
        //schoolDataSource.inviteDataSource.store(listOf(invite))
        return invite
    }

}