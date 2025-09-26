package world.respect.app.view.schooldirectory.list

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import world.respect.datalayer.respect.model.SchoolDirectoryEntry
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.delete_directory
import world.respect.shared.viewmodel.schooldirectory.list.SchoolDirectoryListUIState
import world.respect.shared.viewmodel.schooldirectory.list.SchoolDirectoryListViewModel


@Composable
fun SchoolDirectoryListScreen(
    viewModel: SchoolDirectoryListViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    SchoolDirectoryListScreen(
        uiState = uiState,
        onDeleteClick = { viewModel.onDeleteDirectory(it) }
    )
}

@Composable
fun SchoolDirectoryListScreen(
    uiState: SchoolDirectoryListUIState,
    onDeleteClick: (SchoolDirectoryEntry) -> Unit
) {

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(uiState.schoolDirectory) { directory ->
            SchoolDirectoryListItem(
                directory = directory,
                onDeleteClick = { onDeleteClick(directory) }
            )
        }
    }
}

@Composable
fun SchoolDirectoryListItem(
    directory: SchoolDirectoryEntry,
    onDeleteClick: () -> Unit
) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth(),
        headlineContent = {
            Text(text = directory.self.toString())
        },

        trailingContent = {
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(Res.string.delete_directory)
                )
            }
        }
    )
}