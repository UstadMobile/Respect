package world.respect.shared.viewmodel.assignment.detail

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.update
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.edit
import world.respect.shared.navigation.AssignmentDetail
import world.respect.shared.navigation.AssignmentEdit
import world.respect.shared.navigation.NavCommand
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.FabUiState

class AssignmentDetailViewModel(
    savedStateHandle: SavedStateHandle
) : RespectViewModel(savedStateHandle) {


    private val route: AssignmentDetail = savedStateHandle.toRoute()

    init {
        _appUiState.update {
            it.copy(
                fabState = FabUiState(
                    visible = true,
                    text = Res.string.edit.asUiText(),
                    icon = FabUiState.FabIcon.EDIT,
                    onClick = ::onClickEdit
                )
            )
        }
    }

    fun onClickEdit() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(AssignmentEdit(guid = route.uid))
        )
    }

}