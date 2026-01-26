package world.respect.shared.viewmodel.person.inviteperson
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import io.ktor.http.Url
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
import world.respect.datalayer.school.ext.copyInvite
import world.respect.datalayer.school.ext.isApprovalRequiredNow
import world.respect.datalayer.school.ext.newUserInviteUid
import world.respect.datalayer.school.model.Invite2
import world.respect.datalayer.school.model.PersonRoleEnum
import world.respect.libutil.ext.CHAR_POOL_NUMBERS
import world.respect.libutil.ext.randomString
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.clipboard.SetClipboardStringUseCase
import world.respect.shared.domain.createlink.CreateInviteLinkUseCase
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.invitation
import world.respect.shared.generated.resources.invite_person
import world.respect.shared.navigation.CopyCode
import world.respect.shared.navigation.InvitePerson
import world.respect.shared.navigation.NavCommand
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.AppBarSearchUiState
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes

data class InvitePersonUiState(
    val inviteOptions: InvitePerson.NewUserInviteOptions = InvitePerson.NewUserInviteOptions(null),
    val invite: DataLoadState<Invite2> = DataLoadingState(),
    val inviteUrl: Url? = null,
    val selectedRole: PersonRoleEnum? = null,
    val className: String? = null,
    val schoolName: String? = null,
    val roleOptions: List<PersonRoleEnum> = emptyList()
) {
    val inviteCode: String?
        get() = invite.dataOrNull()?.code

    val approvalRequired: Boolean
        get() = invite.dataOrNull()?.isApprovalRequiredNow() ?: true

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

    private val createInviteLinkUseCase: CreateInviteLinkUseCase by inject()

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

        viewModelScope.launch {
            val currentPersonRole = accountManager.selectedAccountAndPersonFlow.first()
                ?.person?.roles?.first()?.roleEnum ?: return@launch

            val writableRoles = getWritableRolesListUseCase(currentPersonRole)
            val selectedRole = writableRoles.firstOrNull() ?: PersonRoleEnum.STUDENT

            _uiState.update {
                it.copy(
                    roleOptions =  writableRoles,
                    selectedRole = selectedRole
                )
            }

            _inviteUid.value = selectedRole.newUserInviteUid

            _inviteUid.collectLatest { inviteUid ->
                if(inviteUid != null) {
                    schoolDataSource.inviteDataSource.findByUidAsFlow(
                        uid = inviteUid,
                        loadParams = DataLoadParams()
                    ).collect { invite ->
                        _uiState.update { prev ->
                            prev.copy(
                                invite = invite,
                                inviteUrl = invite.dataOrNull()?.let {
                                    createInviteLinkUseCase(it.code)
                                },
                            )
                        }
                    }
                }else {
                    _uiState.update { it.copy(invite = NoDataLoadedState.notFound()) }
                }
            }
        }
    }

    private fun launchUpdateInvite(invite: Invite2) {
        launchWithLoadingIndicator {
            schoolDataSource.inviteDataSource.store(listOf(invite))
        }
    }


    fun copyInviteLinkToClipboard() {
        _uiState.value.invite.dataOrNull()?.code?.also { code ->
            setClipboardStringUseCase(createInviteLinkUseCase(code).toString())
        }
    }

    fun onApprovalEnabledChanged(enabled: Boolean) {
        val currentInvite = _uiState.value.invite.dataOrNull() ?: return
        launchUpdateInvite(
            currentInvite.copyInvite(
                approvalRequiredAfter = if(!enabled) {
                    Clock.System.now() + Invite2.APPROVAL_NOT_REQUIRED_INTERVAL_MINS.minutes
                }else {
                    Clock.System.now()
                }
            )
        )
    }


    fun onClickGetCode() {
        viewModelScope.launch {
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
            _uiState.value.invite.dataOrNull()?.code?.also {
                smsLinkLauncher(createInviteLinkUseCase(it).toString())
            }
        }
    }

    fun onSendLinkViaEmail() {
        viewModelScope.launch {
            _uiState.value.invite.dataOrNull()?.code?.also { code ->
                emailLinkLauncher(
                    subject = getString(Res.string.invitation) ,
                    body= createInviteLinkUseCase(code).toString()
                )
            }
        }
    }

    fun onShareLink() {
        viewModelScope.launch {
            _uiState.value.invite.dataOrNull()?.code?.also { code ->
                shareLinkLauncher(
                    body = createInviteLinkUseCase(code).toString()
                )
            }
        }
    }

    fun onClickResetCode() {
        _uiState.value.invite.dataOrNull()?.also { currentInvite ->
            launchUpdateInvite(
                currentInvite.copyInvite(
                    code = randomString(10, CHAR_POOL_NUMBERS),
                )
            )
        }
    }

}