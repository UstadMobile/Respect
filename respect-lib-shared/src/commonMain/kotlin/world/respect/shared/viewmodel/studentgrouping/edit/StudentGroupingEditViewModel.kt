package world.respect.shared.viewmodel.studentgrouping.edit

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.component.KoinScopeComponent
import org.koin.core.scope.Scope
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.navigation.StudentGroupingEdit
import world.respect.shared.viewmodel.RespectViewModel

data class StudentGroupingEditUiState(
    val str: String=""
)

class StudentGroupingEditViewModel(
    savedStateHandle: SavedStateHandle,
    accountManager: RespectAccountManager,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {
    override val scope: Scope = accountManager.requireActiveAccountScope()

    private val _uiState = MutableStateFlow(StudentGroupingEditUiState())

    val uiState = _uiState.asStateFlow()

    private val route: StudentGroupingEdit = savedStateHandle.toRoute()


}
