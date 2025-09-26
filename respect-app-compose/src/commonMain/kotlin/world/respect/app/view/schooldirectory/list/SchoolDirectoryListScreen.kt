package world.respect.app.view.schooldirectory.list

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import world.respect.shared.viewmodel.schooldirectory.list.SchoolDirectoryListUIState
import world.respect.shared.viewmodel.schooldirectory.list.SchoolDirectoryListViewModel


@Composable
fun SchoolDirectoryListScreen(
    viewModel: SchoolDirectoryListViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    SchoolDirectoryListScreen(
        uiState = uiState
    )
}

@Composable
fun SchoolDirectoryListScreen(uiState: SchoolDirectoryListUIState) {

    LazyColumn(modifier = Modifier.fillMaxSize()) {

    }
}