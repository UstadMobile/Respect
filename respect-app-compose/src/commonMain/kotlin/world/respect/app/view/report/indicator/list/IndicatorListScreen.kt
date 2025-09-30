package world.respect.app.view.report.indicator.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import world.respect.app.components.defaultItemPadding
import world.respect.app.components.respectRememberPager
import world.respect.datalayer.school.model.Indicator
import world.respect.shared.viewmodel.report.indictor.list.IndicatorListUiState
import world.respect.shared.viewmodel.report.indictor.list.IndicatorListViewModel

@Composable
fun IndicatorListScreen(
    viewModel: IndicatorListViewModel
) {
    val uiState: IndicatorListUiState by viewModel.uiState.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .defaultItemPadding()
    ) {
        when {
            uiState.errorMessage != null -> {
                Text(
                    text = "Error: ${uiState.errorMessage}",
                    color = MaterialTheme.colorScheme.error
                )
            }

            else -> {
                IndicatorListContent(
                    uiState = uiState,
                    onItemClick = { indicator ->
                        viewModel.onIndicatorSelected(indicator)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun IndicatorListContent(
    uiState: IndicatorListUiState,
    onItemClick: (Indicator) -> Unit,
    modifier: Modifier = Modifier
) {
    val pager = respectRememberPager(uiState.indicators)
    val lazyPagingItems = pager.flow.collectAsLazyPagingItems()
    LazyColumn(
        modifier = modifier,
    ) {
        items(lazyPagingItems.itemSnapshotList) { indicator ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { indicator?.also({ onItemClick(indicator) }) }
                    .padding(vertical = 12.dp)
            ) {
                Text(
                    text = indicator?.name ?: "",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = indicator?.description ?: "",
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}