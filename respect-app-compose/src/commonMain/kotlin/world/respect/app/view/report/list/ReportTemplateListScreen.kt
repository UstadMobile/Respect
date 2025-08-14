package world.respect.app.view.report.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
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
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.stringResource
import world.respect.app.view.report.graph.CombinedGraph
import world.respect.datalayer.respect.model.RespectReport
import world.respect.shared.domain.report.model.ReportOptions
import world.respect.shared.domain.report.model.RunReportResultAndFormatters
import world.respect.shared.domain.report.query.RunReportUseCase
import world.respect.shared.generated.resources.No_data_available
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.select_template
import world.respect.shared.viewmodel.report.list.ReportTemplateListUiState
import world.respect.shared.viewmodel.report.list.ReportTemplateListViewModel

@Composable
fun ReportTemplateListScreen(
    navController: NavHostController,
    viewModel: ReportTemplateListViewModel
) {
    val uiState: ReportTemplateListUiState by viewModel.uiState.collectAsState(
        ReportTemplateListUiState()
    )

    var reports by remember { mutableStateOf<List<RespectReport>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            val result = uiState.templates().load(
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
            LazyColumn {
                item {
                    ReportTemplateCard(
                        report = RespectReport(
                            reportId = "0",
                            title = stringResource(Res.string.select_template),
                            reportOptions = Json.encodeToString(
                                ReportOptions.serializer(),
                                ReportOptions()
                            ),
                            reportIsTemplate = true,
                        ),
                        viewModel = viewModel,
                        activeUserPersonUid = uiState.activeUserPersonUid
                    )
                }
                items(reports) { report ->
                    ReportTemplateCard(
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
private fun ReportTemplateCard(
    report: RespectReport,
    viewModel: ReportTemplateListViewModel,
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

    Row(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .height(100.dp)  // Fixed height for consistency
            .clickable { viewModel.onTemplateSelected(report) },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .weight(0.3f)
                .fillMaxHeight(),
            contentAlignment = Alignment.CenterStart
        ) {
            if (reportResultWithFormatters.reportResult.results.isEmpty() ||
                reportResultWithFormatters.reportResult.resultSeries.isEmpty()
            ) {
                Text(
                    stringResource(Res.string.No_data_available),
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                CombinedGraph(
                    isSmallSize = true,
                    reportResult = reportResultWithFormatters.reportResult,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = MaterialTheme.colorScheme.surface),
                    xAxisFormatter = reportResultWithFormatters.xAxisFormatter,
                    yAxisFormatter = reportResultWithFormatters.yAxisFormatter
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        // Title above the chart
        Text(
            text = report.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp).weight(1f)
                .weight(2f),
        )
    }
}