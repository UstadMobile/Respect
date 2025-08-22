package world.respect.app.view.report.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.paging.PagingSource
import kotlinx.datetime.TimeZone
import org.jetbrains.compose.resources.stringResource
import world.respect.app.view.report.graph.CombinedGraph
import world.respect.datalayer.respect.model.RespectReport
import world.respect.datalayer.realm.model.report.ReportOptions
import world.respect.shared.domain.report.model.RunReportResultAndFormatters
import world.respect.shared.domain.report.query.RunReportUseCase
import world.respect.shared.generated.resources.No_data_available
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.delete
import world.respect.shared.viewmodel.report.list.ReportListUiState
import world.respect.shared.viewmodel.report.list.ReportListViewModel

@Composable
fun ReportListScreen(
    navController: NavHostController,
    viewModel: ReportListViewModel
) {
    val uiState: ReportListUiState by viewModel.uiState.collectAsState(ReportListUiState())

    var reports by remember { mutableStateOf<List<RespectReport>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            val result = uiState.reportList().load(
                PagingSource.LoadParams.Refresh(
                    key = null,
                    loadSize = 20,
                    placeholdersEnabled = false
                )
            )
            reports = when (result) {
                is PagingSource.LoadResult.Page -> result.data
                else -> emptyList()
            }
        } catch (e: Exception) {
            error = e.message
        } finally {
            isLoading = false
        }
    }

    when {
        isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        error != null -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Error loading reports: $error")
            }
        }

        else -> {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(200.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp)
            ) {
                items(reports) { report ->
                    ReportGridCard(
                        report = report,
                        viewModel = viewModel,
                        activeUserPersonUid = uiState.activeUserPersonUid
                    )
                }
            }
        }
    }
}

@Composable
private fun ReportGridCard(
    report: RespectReport,
    viewModel: ReportListViewModel,
    activeUserPersonUid: Long
) {
    val reportDataFlow = remember(report.reportId) {
        viewModel.runReport(report)
    }
    val reportResultWithFormatters by reportDataFlow.collectAsState(
        initial = RunReportResultAndFormatters(
            reportResult = RunReportUseCase.RunReportResult(
                timestamp = 0,
                request = RunReportUseCase.RunReportRequest(
                    reportUid = report.reportId.toLong(),
                    reportOptions = ReportOptions(),
                    accountPersonUid = activeUserPersonUid,
                    timeZone = TimeZone.currentSystemDefault()
                ),
                results = emptyList()
            ),
            xAxisFormatter = null,
            yAxisFormatter = null
        )
    )

    Card(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
            .clickable {
                viewModel.onClickEntry(report) }
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.12f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box {
            Column(
                modifier = Modifier
                    .padding(4.dp)
                    .fillMaxWidth()
            ) {
                // Title above the chart
                Text(
                    report.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Chart content
                Box(
                    modifier = Modifier
                        .height(200.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    if (reportResultWithFormatters.reportResult.results.isEmpty() ||
                        reportResultWithFormatters.reportResult.resultSeries.isEmpty()) {
                        Text(
                            stringResource(Res.string.No_data_available),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        CombinedGraph(
                            reportResult = reportResultWithFormatters.reportResult,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(color = MaterialTheme.colorScheme.surface),
                            xAxisFormatter = reportResultWithFormatters.xAxisFormatter,
                            yAxisFormatter = reportResultWithFormatters.yAxisFormatter
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Delete icon positioned in top-right corner
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = stringResource(Res.string.delete),
                modifier = Modifier
                    .size(32.dp)
                    .padding(8.dp)
                    .clickable { viewModel.onRemoveReport(report.reportId.toLong()) }
                    .align(Alignment.TopEnd)
            )
        }
    }
}