package world.respect.shared.viewmodel.person.deleteaccount

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.component.KoinScopeComponent
import org.koin.core.scope.Scope
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.navigation.DeleteAccount
import world.respect.shared.viewmodel.RespectViewModel

data class DeleteAccountUiState(
    val accountGuid: String = "",
)
class DeleteAccountViewModel(
    savedStateHandle: SavedStateHandle,
    accountManager: RespectAccountManager,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireSelectedAccountScope()

    private val route: DeleteAccount = savedStateHandle.toRoute()

    private val _uiState = MutableStateFlow(DeleteAccountUiState())

    val uiState = _uiState.asStateFlow()
    fun onDeleteAccount(){

    }


}
