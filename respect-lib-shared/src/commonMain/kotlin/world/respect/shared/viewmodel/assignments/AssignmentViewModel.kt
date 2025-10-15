package world.respect.shared.viewmodel.assignments

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.update
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.assignments
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel

class AssignmentViewModel(
    savedStateHandle: SavedStateHandle
) : RespectViewModel(savedStateHandle) {

    init {
        _appUiState.update {
            it.copy(
                title = Res.string.assignments.asUiText(),
            )
        }
    }
}