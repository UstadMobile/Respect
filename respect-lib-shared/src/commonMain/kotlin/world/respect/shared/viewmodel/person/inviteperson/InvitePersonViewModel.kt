package world.respect.shared.viewmodel.person.inviteperson
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.ustadmobile.libcache.sharelink.EmailLinkLauncher
import com.ustadmobile.libcache.sharelink.ShareLinkLauncher
import com.ustadmobile.libcache.sharelink.SmsLinkLauncher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.school.model.Clazz.Companion.DEFAULT_INVITE_CODE_LEN
import world.respect.datalayer.school.model.Clazz.Companion.DEFAULT_INVITE_CODE_MAX
import world.respect.datalayer.school.model.Invite
import world.respect.datalayer.school.model.PersonRoleEnum
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.clipboard.SetClipboardStringUseCase
import world.respect.shared.domain.createlink.CreateLinkUseCase
import world.respect.shared.domain.school.SchoolPrimaryKeyGenerator
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.invite_person
import world.respect.shared.navigation.CopyCode
import world.respect.shared.navigation.InvitePerson
import world.respect.shared.navigation.NavCommand
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.AppBarSearchUiState
import kotlin.random.Random
import kotlin.time.Clock

data class InvitePersonUiState(
    val inviteCode: String? = null,
    val inviteMultipleAllowed: Boolean = false,
    val approvalRequired: Boolean = false,
    val selectedRole: PersonRoleEnum? = null,
    val shareLink: String? = null,
    val classGuid: String? = null,
    val familyPersonGuid: String? = null,
    val roleOptions: List<PersonRoleEnum> = emptyList(),
    val invite: Invite? = null
)

class InvitePersonViewModel(
    savedStateHandle: SavedStateHandle,
    private val accountManager: RespectAccountManager,
    private val setClipboardStringUseCase: SetClipboardStringUseCase,
    private val smsLinkLauncher: SmsLinkLauncher,
    private val shareLinkLauncher: ShareLinkLauncher,
    private val emailLinkLauncher: EmailLinkLauncher
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    private val route: InvitePerson = savedStateHandle.toRoute()

    override val scope: Scope = accountManager.requireSelectedAccountScope()

    private val createLinkUseCase: CreateLinkUseCase by lazy {
        scope.get()
    }

    private val schoolPrimaryKeyGenerator: SchoolPrimaryKeyGenerator by inject()

    private val guid = schoolPrimaryKeyGenerator.primaryKeyGenerator.nextId(
        Invite.TABLE_ID
    ).toString()
    private val schoolDataSource: SchoolDataSource by inject()

    private val _uiState = MutableStateFlow(InvitePersonUiState())
    val uiState = _uiState.asStateFlow()

    init {

        _uiState.update { prev ->
            prev.copy(
                classGuid = route.classGuid,
                familyPersonGuid = route.familyPersonGuid
            )
        }
        _appUiState.update {
            it.copy(
                title = Res.string.invite_person.asUiText(),
                searchState = AppBarSearchUiState(visible = false),
                showBackButton = true,
                hideBottomNavigation = true,
                userAccountIconVisible = false
            )
        }
        launchWithLoadingIndicator {
            val currentPersonRole = accountManager.selectedAccountAndPersonFlow.first()
                ?.person?.roles?.first()?.roleEnum

            _uiState.update { prev ->
                prev.copy(
                    roleOptions =
                        when (currentPersonRole) {
                            PersonRoleEnum.TEACHER -> listOf(
                                PersonRoleEnum.STUDENT,
                                PersonRoleEnum.PARENT,
                                PersonRoleEnum.TEACHER,
                            )

                            PersonRoleEnum.SITE_ADMINISTRATOR, PersonRoleEnum.SYSTEM_ADMINISTRATOR -> listOf(
                                PersonRoleEnum.STUDENT,
                                PersonRoleEnum.PARENT,
                                PersonRoleEnum.TEACHER,
                                PersonRoleEnum.SYSTEM_ADMINISTRATOR,
                            )

                            else -> emptyList()
                        }
                )
            }
        }
    }

    fun copyInviteCodeToClipboard() {
        viewModelScope.launch {
          createOrEditInvite()
            _uiState.value.inviteCode?.also { code ->
                setClipboardStringUseCase(code)
            }
        }
    }

    fun setInviteMultipleAllowed(enabled: Boolean) {
        _uiState.update { it.copy(inviteMultipleAllowed = enabled) }
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
            emailLinkLauncher.launch(link)
        }
    }

    fun onShareLink() {
        viewModelScope.launch {
            val link =  createOrEditInvite()
            shareLinkLauncher.launch(link)
        }
    }


    private fun generateCode(): String {
        return Random.nextInt(DEFAULT_INVITE_CODE_MAX)
            .toString()
            .padStart(DEFAULT_INVITE_CODE_LEN, '0')
    }

    private suspend fun storeInvite(invite: Invite): Invite {
        schoolDataSource.inviteDataSource.store(listOf(invite))
        return invite
    }

    private suspend fun createOrEditInvite(): String {
        val newInvite = createOrUpdateInviteInternal()
        val link = createLinkUseCase(newInvite.code)
        _uiState.update {
            it.copy(
                shareLink = link,
                invite = newInvite,
                inviteCode = newInvite.code
            )
        }
        return link
    }

    private suspend fun createOrUpdateInviteInternal(): Invite {
        val current =uiState.value.invite
        if (current == null) {
            val code = uiState.value.inviteCode ?: generateCode()
            val invite = Invite(
                guid = guid,
                code = code,
                newRole = uiState.value.selectedRole,
                inviteMultipleAllowed = uiState.value.inviteMultipleAllowed,
                approvalRequired = uiState.value.approvalRequired,
                forClassGuid = uiState.value.classGuid,
                forFamilyOfGuid = uiState.value.familyPersonGuid,
            )
            return storeInvite(invite)
        } else {
            val changed = (current.approvalRequired != uiState.value.approvalRequired)
                    || (current.newRole !=uiState.value.selectedRole)
                    || (current.inviteMultipleAllowed != uiState.value.inviteMultipleAllowed)
            if (!changed) return current
            val updated = current.copy(
                newRole =uiState.value.selectedRole,
                inviteMultipleAllowed =uiState.value.inviteMultipleAllowed,
                approvalRequired =uiState.value.approvalRequired,
                lastModified = Clock.System.now(),
                stored = Clock.System.now()
            )
            return storeInvite(updated)
        }
    }


}