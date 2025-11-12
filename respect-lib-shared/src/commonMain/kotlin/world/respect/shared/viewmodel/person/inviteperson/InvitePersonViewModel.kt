package world.respect.shared.viewmodel.person.inviteperson
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.datalayer.SchoolDataSource
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.clipboard.SetClipboardStringUseCase
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.invite_person
import world.respect.shared.navigation.NavResultReturner
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.AppBarSearchUiState

data class InvitePersonUiState(
    val inviteCode: String? = null,
    val inviteMultipleAllowed: Boolean = false,
    val approvalRequired: Boolean = false,
    val selectedRole: String? = null,
    val shareLink: String? = null
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

    fun selectRole(role: String) {
        _uiState.update { it.copy(selectedRole = role) }
    }

    fun setShareLink(link: String) {
        _uiState.update { it.copy(shareLink = link) }
    }

    fun onSendLinkViaSms() {
        viewModelScope.launch {
            val link = _uiState.value.shareLink ?: _uiState.value.inviteCode ?: return@launch
        }
    }

    fun onSendLinkViaEmail() {
        viewModelScope.launch {
            val link = _uiState.value.shareLink ?: _uiState.value.inviteCode ?: return@launch
        }
    }

    fun onShareLink() {
        viewModelScope.launch {
            val link = _uiState.value.shareLink ?: _uiState.value.inviteCode ?: return@launch
        }
    }

    fun regenerateInviteCode() {
        viewModelScope.launch {
            val newCode = "NEW-CODE-${System.currentTimeMillis()}"
            _uiState.update { it.copy(inviteCode = newCode, shareLink = "https://example.com/invite/$newCode") }
        }
    }

    fun onDone() {
        viewModelScope.launch { }
    }
}