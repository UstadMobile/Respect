package world.respect.app.view.report.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ImportExport
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import kotlinx.coroutines.Dispatchers
import org.jetbrains.compose.resources.stringResource
import world.respect.app.view.report.graph.CombinedGraph
import world.respect.app.view.report.graph.asString
import world.respect.shared.domain.report.formatter.GraphFormatter
import world.respect.shared.domain.report.model.ReportSeries
import world.respect.shared.domain.report.model.StatementReportRow
import world.respect.shared.domain.report.query.RunReportUseCase
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.empty_data
import world.respect.shared.generated.resources.export_data
import world.respect.shared.generated.resources.graph_data
import world.respect.shared.generated.resources.raw_data
import world.respect.shared.generated.resources.share
import world.respect.shared.generated.resources.subgroup_by
import world.respect.shared.generated.resources.x_axis
import world.respect.shared.generated.resources.y_axis
import world.respect.shared.viewmodel.report.detail.ReportDetailUiState
import world.respect.shared.viewmodel.report.detail.ReportDetailViewModel

@Composable
fun ReportDetailScreen(
    navController: NavHostController,
    viewModel: ReportDetailViewModel
) {
    val uiState: ReportDetailUiState by viewModel.uiState.collectAsStateWithLifecycle(
        initialValue = ReportDetailUiState(),
        context = Dispatchers.Main.immediate
    )
    ReportDetailScreen(
        uiState = uiState,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailScreen(
    uiState: ReportDetailUiState
) {
    val sheetState = rememberModalBottomSheetState()
    var showExportOptions by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        if (uiState.reportResult?.results?.isNotEmpty() == true) {
            val reportResult = uiState.reportResult
            if (reportResult != null) {
                CombinedGraph(
                    reportResult = reportResult,
                    modifier = Modifier.weight(0.6f),
                    xAxisFormatter = uiState.xAxisFormatter,
                    yAxisFormatter = uiState.yAxisFormatter
                )
                MoreOptionsSection(
                    seriesList = reportResult.resultSeries,
                    modifier = Modifier.weight(0.4f),
                    xAxisFormatter = uiState.xAxisFormatter,
                    yAxisFormatter = uiState.yAxisFormatter,
                    subgroupFormatter = uiState.subgroupFormatter,
                    onExportClick = { showExportOptions = true }
                )
            }
        } else {
            EmptyDataMessage(Modifier.weight(1f))
        }
    }

    if (showExportOptions) {
        ModalBottomSheet(
            onDismissRequest = { showExportOptions = false },
            sheetState = sheetState
        ) {
            ExportOptionsBottomSheet(
                onGraphFormatClick = {
                    // Handle graph format export
                    showExportOptions = false
                },
                onRawDataClick = {
                    // Handle raw data export
                    showExportOptions = false
                }
            )
        }
    }
}


@Composable
private fun EmptyDataMessage(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(stringResource(Res.string.empty_data))
    }
}

@Composable
fun MoreOptionsSection(
    seriesList: List<RunReportUseCase.RunReportResult.Series>,
    modifier: Modifier = Modifier,
    xAxisFormatter: GraphFormatter<String>?,
    yAxisFormatter: GraphFormatter<Double>?,
    subgroupFormatter: GraphFormatter<String>?,
    onExportClick: () -> Unit = {}
) {
    LazyColumn(modifier = modifier.fillMaxWidth()) {
        item { HorizontalDivider(thickness = 1.dp) }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Share Option
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = "Share",
                        modifier = Modifier.size(24.dp)
                    )
                    Text(text = stringResource(Res.string.share))
                }

                // Export Option
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onExportClick() }
                ) {
                    Icon(
                        imageVector = Icons.Filled.ImportExport,
                        contentDescription = "Export data",
                        modifier = Modifier.size(24.dp)
                    )
                    Text(text = stringResource(Res.string.export_data))
                }
            }
        }

        item { HorizontalDivider(thickness = 1.dp) }

        items(seriesList) { series ->
            DataTable(
                data = series.data,
                reportSeries = series.reportSeriesOptions,
                xAxisFormatter = xAxisFormatter,
                yAxisFormatter = yAxisFormatter,
                subgroupFormatter = subgroupFormatter
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ExportOptionsBottomSheet(
    onGraphFormatClick: () -> Unit,
    onRawDataClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(Res.string.graph_data),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.clickable { onGraphFormatClick() }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(Res.string.raw_data),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.clickable { onRawDataClick() }
        )
    }
}

@Composable
fun DataTable(
    data: List<StatementReportRow>,
    reportSeries: ReportSeries,
    xAxisFormatter: GraphFormatter<String>?,
    yAxisFormatter: GraphFormatter<Double>?,
    subgroupFormatter: GraphFormatter<String>?,
) {
    val subgroupName = reportSeries.reportSeriesSubGroup?.label?.let { stringResource(it) }
    val subgroupByStr = stringResource(Res.string.subgroup_by)

    val subgroupLabel = remember(subgroupName) {
        if (subgroupName != null) {
            "$subgroupByStr - $subgroupName"
        } else {
            subgroupByStr
        }
    }

    val header = listOf(
        stringResource(Res.string.x_axis),
        stringResource(Res.string.y_axis),
        subgroupLabel
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.LightGray)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                header.forEachIndexed { index, title ->
                    Text(
                        text = title,
                        modifier = Modifier.weight(1f),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    if (index < header.lastIndex) {
                        VerticalDivider(
                            modifier = Modifier.height(24.dp),
                            color = Color.DarkGray,
                            thickness = 1.dp
                        )
                    }
                }
            }

            HorizontalDivider(color = Color.DarkGray, thickness = 1.dp)

            data.forEach { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Format X-axis value
                    val xAxisValue = xAxisFormatter?.format(row.xAxis)?.asString() ?: row.xAxis
                    Text(
                        text = xAxisValue,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    VerticalDivider(
                        modifier = Modifier.height(24.dp),
                        color = Color.LightGray,
                        thickness = 1.dp
                    )

                    // Format Y-axis value
                    val yAxisValue = yAxisFormatter?.let {
                        val adjustedValue = it.adjust(row.yAxis)
                        it.format(adjustedValue).asString()
                    } ?: row.yAxis.toString()
                    Text(
                        text = yAxisValue,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    VerticalDivider(
                        modifier = Modifier.height(24.dp),
                        color = Color.LightGray,
                        thickness = 1.dp
                    )

                    // Format subgroup value
                    val subgroupValue = subgroupFormatter?.let {
                        val adjustedValue = it.adjust(row.subgroup)
                        it.format(adjustedValue).asString()
                    } ?: row.subgroup
                    Text(
                        text = subgroupValue,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                HorizontalDivider(color = Color.LightGray)
            }
        }
    }
}