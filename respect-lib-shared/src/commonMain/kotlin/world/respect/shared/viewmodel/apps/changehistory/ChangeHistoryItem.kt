package world.respect.shared.viewmodel.apps.changehistory

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.component.KoinScopeComponent
import org.koin.core.scope.Scope
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.change_history
import world.respect.shared.navigation.ChangeHistory
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.FabUiState


data class ChangeHistoryUiState(
    val guid: String = "",
)

class ChangeHistoryViewModel(
    savedStateHandle: SavedStateHandle,
    accountManager: RespectAccountManager
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()

    private val route: ChangeHistory = savedStateHandle.toRoute()

    private val _uiState = MutableStateFlow(
        ChangeHistoryUiState(
            guid = route.guid
        )
    )

    val uiState = _uiState.asStateFlow()

    init {
        _appUiState.update { prev ->
            prev.copy(
                title = Res.string.change_history.asUiText(),
                fabState = FabUiState()
            )
        }
    }

}