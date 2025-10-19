package world.respect.app.view.assignment.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import world.respect.shared.viewmodel.assignment.detail.AssignmentDetailUiState
import world.respect.shared.viewmodel.assignment.detail.AssignmentDetailViewModel

@Composable
fun AssignmentDetailScreen(
    viewModel: AssignmentDetailViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    AssignmentDetailScreen(uiState)
}

fun AssignmentDetailScreen(
    uiState: AssignmentDetailUiState,
) {

}