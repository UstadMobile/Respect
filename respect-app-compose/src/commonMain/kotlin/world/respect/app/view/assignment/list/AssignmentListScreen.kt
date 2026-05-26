package world.respect.app.view.assignment.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import world.respect.app.components.defaultItemPadding
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.lib.xapi.model.AssignmentSummary
import world.respect.shared.util.AssignmentListScreenFilter
import world.respect.shared.viewmodel.assignment.list.AssignmentListUiState
import world.respect.shared.viewmodel.assignment.list.AssignmentListViewModel


@Composable
fun AssignmentListScreen(
    viewModel: AssignmentListViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    AssignmentListScreen(
        uiState = uiState,
        onFilterSelected = viewModel::onFilterChanged,
        onClickAssignment = viewModel::onClickAssignment,
    )
}

@Composable
fun AssignmentListScreen(
    uiState: AssignmentListUiState,
    onFilterSelected: (AssignmentListScreenFilter) -> Unit,
    onClickAssignment: (AssignmentSummary) -> Unit = { },
) {
    val assignments = uiState.assignments.dataOrNull() ?: emptyList()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultItemPadding(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AssignmentListScreenFilter.entries.forEach { filter ->
                FilterChip(
                    selected = uiState.selectedFilter == filter,
                    onClick = { onFilterSelected(filter) },
                    label = { Text(uiState.getLabelForFilter(filter)) },
                    shape = RoundedCornerShape(50)
                )
            }
        }
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(
                items = assignments,
            ) { summary ->
                AssignmentListItem(
                    summary = summary,
                    onClick = onClickAssignment,
                )
            }
        }
    }
}
