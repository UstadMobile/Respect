package world.respect.shared.viewmodel.statement.list

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.component.KoinScopeComponent
import org.koin.core.scope.Scope
import world.respect.lib.xapi.model.XapiStatement
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.list
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel


data class StatementListUiState(
    val statements: List<XapiStatement> = emptyList(),
)

class StatementListViewModel(
    savedStateHandle: SavedStateHandle,
    accountManager: RespectAccountManager,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()

    private val _uiState = MutableStateFlow(StatementListUiState())

    val uiState: StateFlow<StatementListUiState> = _uiState.asStateFlow()

    init {
        _appUiState.update {
            it.copy(
                title = Res.string.list.asUiText(),
                showBackButton = true,
                hideBottomNavigation = true
            )
        }
    }
}