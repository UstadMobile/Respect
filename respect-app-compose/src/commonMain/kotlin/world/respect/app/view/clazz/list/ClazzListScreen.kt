package world.respect.app.view.clazz.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import world.respect.app.components.RespectPersonAvatar
import world.respect.app.components.RespectSortHeader
import world.respect.shared.viewmodel.clazz.list.ClazzListUiState
import world.respect.shared.viewmodel.clazz.list.ClazzListViewModel

@Composable
fun ClazzListScreen(
    viewModel: ClazzListViewModel
) {

    val uiState by viewModel.uiState.collectAsState()

    ClazzListScreen(
        uiState = uiState,
        onClickClazz = viewModel::onClickClazz,
        onClickSortOption = { viewModel.onClickSortOption(it) },
    )
}

@Composable
fun ClazzListScreen(
    uiState: ClazzListUiState,
    onClickClazz: () -> Unit,
    onClickSortOption: (String) -> Unit,
) {

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {

        item {
            RespectSortHeader(
                options = uiState.sortOptions.map { it.option },
                selectedOption = uiState.selectedSortOption,
                onOptionSelected = onClickSortOption,
                optionLabel = { it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }

        itemsIndexed(
            uiState.oneRoasterClass,
            key = { index, clazz -> index }
        ) { index, clazz ->
            ListItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onClickClazz()
                    },

                leadingContent = {
                    RespectPersonAvatar(
                        name = clazz.title,
                        modifier = Modifier.size(40.dp),
                        fontScale = 1.0f
                    )
                },

                headlineContent = {
                    Text(
                        text = clazz.title
                    )
                }
            )
        }
    }
}