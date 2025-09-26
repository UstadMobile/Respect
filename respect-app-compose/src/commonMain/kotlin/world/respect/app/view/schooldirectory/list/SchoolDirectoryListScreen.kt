package world.respect.app.view.schooldirectory.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
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
import world.respect.datalayer.respect.model.RespectSchoolDirectory
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
    onDeleteClick: (RespectSchoolDirectory) -> Unit
) {

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(uiState.schoolDirectory.size) { index ->
            val directory = uiState.schoolDirectory[index]
            SchoolDirectoryListItem(
                directory = directory,
                onClick = { /* Handle item click */ },
                onDeleteClick = { onDeleteClick(directory) }
            )
        }
    }
}

@Composable
fun SchoolDirectoryListItem(
    directory: RespectSchoolDirectory,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },

        headlineContent = {
            Text(text = directory.baseUrl.toString())
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