package world.respect.app.view.statement.list

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.shared.viewmodel.statement.list.StatementListUiState
import world.respect.shared.viewmodel.statement.list.StatementListViewModel

@Composable
fun StatementListScreen(
    viewModel: StatementListViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    StatementListScreen(
        uiState = uiState,
        onClickListItem = viewModel::onClickListItem
    )
}

@Composable
fun StatementListScreen(
    uiState: StatementListUiState,
    onClickListItem: (statementId: String) -> Unit = {},
) {
    val statements = uiState.statements.dataOrNull() ?: emptyList()
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(statements) { statement ->
            StatementListItem(statement = statement, onClickListItem = onClickListItem)
        }
    }
}
