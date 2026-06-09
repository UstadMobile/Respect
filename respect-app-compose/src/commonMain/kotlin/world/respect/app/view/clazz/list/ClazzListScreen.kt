package world.respect.app.view.clazz.list

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
import world.respect.app.components.RespectEmptyListComponent
import world.respect.app.components.RespectListSortHeader
import world.respect.app.components.RespectPersonAvatar
import world.respect.app.components.SortListMode
import world.respect.app.components.defaultItemPadding
import world.respect.app.components.defaultSortListMode
import world.respect.app.components.langMapString
import world.respect.lib.xapi.ext.objectActivityOrNull
import world.respect.lib.xapi.model.XapiActivity
import world.respect.lib.xapi.model.XapiStatement
import world.respect.shared.util.SortOrderOption
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
        onClickSortOption = viewModel::onSortOrderChanged,
    )
}

@Composable
fun ClazzListScreen(
    uiState: ClazzListUiState,
    onClickClazz: (XapiStatement) -> Unit,
    onClickSortOption: (SortOrderOption) -> Unit = { },
    sortListMode: SortListMode = defaultSortListMode(),
) {

    LazyColumn(modifier = Modifier.fillMaxSize()) {

        item("header") {
            RespectListSortHeader(
                modifier = Modifier.defaultItemPadding(),
                activeSortOrderOption = uiState.activeSortOrderOption,
                sortOptions = uiState.sortOptions,
                enabled = uiState.fieldsEnabled,
                onClickSortOption = onClickSortOption,
                mode = sortListMode,
            )
        }

        items(
            items = uiState.classStatements,
            key = { statement ->
                (statement.`object` as? XapiActivity)?.id ?: statement.id.toString()
            },
        ) { statement ->
            val title = statement.objectActivityOrNull()?.definition?.name
                ?.takeIf { it.isNotEmpty() }?.let { langMapString(it) } ?:""
            ListItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onClickClazz(statement)
                    },

                leadingContent = {
                    RespectPersonAvatar(name = title)
                },

                headlineContent = {
                    Text(text = title)
                }
            )
        }

        if(uiState.classStatements.isEmpty()) {
            item("empty_item") {
                RespectEmptyListComponent(Modifier.fillMaxWidth().defaultItemPadding())
            }
        }

    }
}