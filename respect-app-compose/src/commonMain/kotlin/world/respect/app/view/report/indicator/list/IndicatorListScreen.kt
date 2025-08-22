package world.respect.app.view.report.indicator.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import world.respect.datalayer.respect.model.Indicator
import world.respect.shared.viewmodel.report.indictor.list.IndicatorListUiState
import world.respect.shared.viewmodel.report.indictor.list.IndicatorListViewModel

@Composable
fun IndicatorListScreen(
    navController: NavHostController,
    viewModel: IndicatorListViewModel
) {
    val uiState: IndicatorListUiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        when {
            uiState.isLoading -> {
                Text("Loading indicators...")
            }

            uiState.errorMessage != null -> {
                Text("Error: ${uiState.errorMessage}", color = Color.Red)
            }

            else -> {
                IndicatorListContent(
                    indicators = uiState.indicators,
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
    indicators: List<Indicator>,
    onItemClick: (Indicator) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
    ) {
        items(indicators, key = { it.name }) { indicator ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onItemClick(indicator) }
                    .padding(vertical = 12.dp)
            ) {
                Text(
                    text = indicator.name,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = indicator.description,
                    color = Color.Gray
                )
            }
        }
    }
}