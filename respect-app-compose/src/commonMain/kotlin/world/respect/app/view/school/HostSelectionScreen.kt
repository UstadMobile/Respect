package world.respect.app.view.school

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import world.respect.datalayer.respect.model.RespectSchoolDirectory
import world.respect.shared.viewmodel.school.HostSelectionListUiState
import world.respect.shared.viewmodel.school.HostSelectionViewModel

@Composable
fun HostSelectionScreen(
    viewModel: HostSelectionViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    HostSelectionScreen(
        uiState = uiState,
        onDirectorySelected = viewModel::onClickNext
    )
}

@Composable
fun HostSelectionScreen(
    uiState: HostSelectionListUiState,
    onDirectorySelected: (RespectSchoolDirectory) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(uiState.schoolDirectory) { directory ->
            DirectoryListItem(
                directory = directory,
                onDirectorySelected = { onDirectorySelected(directory) }
            )
        }
    }
}

@Composable
fun DirectoryListItem(
    directory: RespectSchoolDirectory,
    onDirectorySelected: () -> Unit
) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onDirectorySelected() },
        headlineContent = {
            Text(text = directory.baseUrl.toString())
        }
    )
}