package world.respect.app.view.statement.list

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import world.respect.shared.viewmodel.statement.list.StatementListUiState
import world.respect.shared.viewmodel.statement.list.StatementListViewModel

@Composable
fun StatementListScreen(
    viewModel: StatementListViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    StatementListScreen(
        uiState = uiState,
    )
}

@Composable
fun StatementListScreen(
    uiState: StatementListUiState
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(uiState.statements) { statement ->
        }
    }
}
