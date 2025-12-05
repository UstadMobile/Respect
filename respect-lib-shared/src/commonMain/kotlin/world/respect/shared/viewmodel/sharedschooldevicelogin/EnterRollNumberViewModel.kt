package world.respect.shared.viewmodel.sharedschooldevicelogin

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.component.KoinScopeComponent
import org.koin.core.scope.Scope
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.enter_roll_number
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel

data class EnterRollNumberUiState(
    val loading: Boolean = false,
    )
class EnterRollNumberViewModel (
    savedStateHandle: SavedStateHandle,
    accountManager: RespectAccountManager,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {
    override val scope: Scope = accountManager.requireSelectedAccountScope()
    private val _uiState = MutableStateFlow(EnterRollNumberUiState())
    val uiState: Flow<EnterRollNumberUiState> = _uiState.asStateFlow()
    init {
        _appUiState.update { prev ->
            prev.copy(
                title = Res.string.enter_roll_number.asUiText(),
                navigationVisible = true,
                hideBottomNavigation = true,
                settingsIconVisible = false
            )
        }
    }
}