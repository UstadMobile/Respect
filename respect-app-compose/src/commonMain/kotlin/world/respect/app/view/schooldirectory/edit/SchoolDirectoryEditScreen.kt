package world.respect.app.view.schooldirectory.edit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import world.respect.shared.viewmodel.schooldirectory.edit.SchoolDirectoryEditUIState
import world.respect.shared.viewmodel.schooldirectory.edit.SchoolDirectoryEditViewModel


@Composable
fun SchoolDirectoryEditScreen(
    viewModel: SchoolDirectoryEditViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    SchoolDirectoryEditScreen(
        uiState = uiState
    )
}

@Composable
fun SchoolDirectoryEditScreen(uiState: SchoolDirectoryEditUIState) {

}