package world.respect.shared.viewmodel.person.inviteperson
import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.school.model.PersonRoleEnum
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.clipboard.SetClipboardStringUseCase
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.invite_person
import world.respect.shared.navigation.CopyCode
import world.respect.shared.navigation.NavCommand
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.AppBarSearchUiState

data class InvitePersonUiState(
    val inviteCode: String? = null,
    val inviteMultipleAllowed: Boolean = false,
    val approvalRequired: Boolean = false,
    val selectedRole: String? = null,
    val shareLink: String? = null,
    val roleOptions: List<PersonRoleEnum> = emptyList(),
)

class InvitePersonViewModel(
    savedStateHandle: SavedStateHandle,
    accountManager: RespectAccountManager,
    private val setClipboardStringUseCase: SetClipboardStringUseCase
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireSelectedAccountScope()

    private val schoolDataSource: SchoolDataSource by inject()

    private val _uiState = MutableStateFlow(InvitePersonUiState())
    val uiState = _uiState.asStateFlow()

    init {
        val codeFromArgs = savedStateHandle.get<String>("inviteCode")
        _uiState.update { it.copy(inviteCode = codeFromArgs) }

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
        _uiState.value.inviteCode?.also { code ->
            setClipboardStringUseCase(code)
        }
    }

    fun setInviteMultipleAllowed(enabled: Boolean) {
        _uiState.update { it.copy(inviteMultipleAllowed = enabled) }
    }

    fun setApprovalRequired(enabled: Boolean) {
        _uiState.update { it.copy(approvalRequired = enabled) }
    }


    fun onClickGetCode() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                CopyCode
            )
        )
    }

    fun onRoleChange(role: PersonRoleEnum){

    }

    fun onSendLinkViaSms() {

    }

    fun onSendLinkViaEmail() {

    }

    fun onShareLink() {

    }

}