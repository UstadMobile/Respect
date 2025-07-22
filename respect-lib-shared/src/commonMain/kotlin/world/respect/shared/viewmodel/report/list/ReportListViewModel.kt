package world.respect.shared.viewmodel.report.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import app.cash.paging.PagingSource
import app.cash.paging.PagingSourceLoadParams
import app.cash.paging.PagingSourceLoadResult
import app.cash.paging.PagingState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.getString
import world.respect.datalayer.db.shared.entities.Report
import world.respect.shared.domain.report.formatter.CreateGraphFormatterUseCase
import world.respect.shared.domain.report.formatter.GraphFormatter
import world.respect.shared.domain.report.model.ReportOptions
import world.respect.shared.domain.report.model.ReportPeriodOption
import world.respect.shared.domain.report.model.ReportSeries
import world.respect.shared.domain.report.model.ReportSeriesVisualType
import world.respect.shared.domain.report.model.ReportSeriesYAxis
import world.respect.shared.domain.report.model.ReportXAxis
import world.respect.shared.domain.report.model.RunReportResultAndFormatters
import world.respect.shared.domain.report.model.StatementReportRow
import world.respect.shared.domain.report.query.RunReportUseCase
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.report
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.ReportDetail
import world.respect.shared.navigation.ReportEdit
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.FabUiState
import world.respect.shared.viewmodel.app.appstate.LoadingUiState.Companion.NOT_LOADING
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

data class ReportListUiState(
    val reportList: () -> PagingSource<Int, Report> = { EmptyPagingSource() },
    val addSheetOrDialogVisible: Boolean = false,
    val activeUserPersonUid: Long = 0L,
    val xAxisFormatter: GraphFormatter<String>? = null,
    val yAxisFormatter: GraphFormatter<Double>? = null
)

class ReportListViewModel(
    savedStateHandle: SavedStateHandle,
    private val runReportUseCase: RunReportUseCase,
    private val createGraphFormatterUseCase: CreateGraphFormatterUseCase
) : RespectViewModel(savedStateHandle) {

    private val _uiState = MutableStateFlow(ReportListUiState())
    val uiState: Flow<ReportListUiState> = _uiState.asStateFlow()
    private val activeUserPersonUid: Long = 0
    private val pagingSourceFactory: () -> PagingSource<Int, Report> = { DummyReportPagingSource() }

    init {
        viewModelScope.launch {
            _appUiState.update { prev ->
                prev.copy(
                    navigationVisible = true,
                    title = getString(resource = Res.string.report),
                    fabState = FabUiState(
                        text = getString(resource = Res.string.report),
                        icon = FabUiState.FabIcon.ADD,
                        onClick = { this@ReportListViewModel.onClickAdd() },
                    )
                )
            }

            _appUiState.update { prev ->
                prev.copy(
                    fabState = prev.fabState.copy(
                        visible = true
                    )
                )
            }
            _uiState.update { prev ->
                prev.copy(
                    reportList = pagingSourceFactory,
                    activeUserPersonUid = activeUserPersonUid
                )
            }

        }
    }

    @OptIn(ExperimentalTime::class)
    fun runReport(report: Report): Flow<RunReportResultAndFormatters> = flow {
        try {
            val reportOptions = Json.decodeFromString<ReportOptions>(
                ReportOptions.serializer(),
                report.reportOptions ?: ""
            )

            // Generate realistic dummy data for the last 7 days
            val daysOfWeek = listOf(
                "15-06-2025",
                "16-06-2025",
                "17-06-2025",
                "18-06-2025",
                "19-06-2025",
                "20-06-2025",
                "21-06-2025"
            )
            val dummyResults = daysOfWeek.mapIndexed { dayIndex, dayName ->
                reportOptions.series.map { series ->
                    StatementReportRow(
                        xAxis = dayName,
                        subgroup = series.reportSeriesTitle,
                        yAxis = when (series.reportSeriesYAxis) {
                            ReportSeriesYAxis.TOTAL_DURATION -> (4..12).random() * 3600.0 // 4-12 hours
                            ReportSeriesYAxis.AVERAGE_DURATION -> (30..120).random() * 60.0 // 30-120 minutes
                            ReportSeriesYAxis.NUMBER_SESSIONS -> (1..10).random()
                                .toDouble() // 1-10 sessions
                            else -> (1..100).random().toDouble()
                        }
                    )
                }
            }

            val reportResult = RunReportUseCase.RunReportResult(
                timestamp = Clock.System.now().toEpochMilliseconds(),
                request = RunReportUseCase.RunReportRequest(
                    reportUid = report.reportUid,
                    reportOptions = reportOptions,
                    accountPersonUid = activeUserPersonUid,
                    timeZone = TimeZone.currentSystemDefault()
                ),
                results = dummyResults,
            )

            // Create formatters for the graph axes
            val xAxisFormatter = createGraphFormatterUseCase(
                reportResult = reportResult,
                options = CreateGraphFormatterUseCase.FormatterOptions(
                    paramType = String::class,
                    axis = CreateGraphFormatterUseCase.FormatterOptions.Axis.X_AXIS_VALUES
                )
            )

            val yAxisFormatter = createGraphFormatterUseCase(
                reportResult = reportResult,
                options = CreateGraphFormatterUseCase.FormatterOptions(
                    paramType = Double::class,
                    axis = CreateGraphFormatterUseCase.FormatterOptions.Axis.Y_AXIS_VALUES
                )
            )

            // Emit the combined result
            emit(
                RunReportResultAndFormatters(
                    reportResult = reportResult,
                    xAxisFormatter = xAxisFormatter,
                    yAxisFormatter = yAxisFormatter
                )
            )

        } catch (e: Exception) {
            // Handle errors and emit an empty result with error state
            val errorResult = RunReportUseCase.RunReportResult(
                timestamp = Clock.System.now().toEpochMilliseconds(),
                request = RunReportUseCase.RunReportRequest(
                    reportUid = report.reportUid,
                    reportOptions = ReportOptions(), // Empty options in error case
                    accountPersonUid = activeUserPersonUid,
                    timeZone = TimeZone.currentSystemDefault()
                ),
                results = emptyList()
            )

            emit(
                RunReportResultAndFormatters(
                    reportResult = errorResult,
                    xAxisFormatter = null,
                    yAxisFormatter = null
                )
            )
            throw e
        } finally {
            _appUiState.update { it.copy(loadingState = NOT_LOADING) }
        }
    }

    fun onClickAdd() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                ReportEdit.create(0L)
            )
        )

    }

    fun onClickEntry(entry: Report) {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                ReportDetail.create(entry.reportUid)
            )
        )
    }

    fun onRemoveReport(uid: Long) {
        // Implement remove functionality
    }
}


class DummyReportPagingSource : PagingSource<Int, Report>() {
    override suspend fun load(params: PagingSourceLoadParams<Int>): PagingSourceLoadResult<Int, Report> {
        val dummyReports = List(3) { index ->
            Report(
                reportUid = index.toLong(),
                reportTitle = "Report ${index + 1}",
                reportOptions = Json.encodeToString(
                    ReportOptions.serializer(),
                    ReportOptions(
                        title = "Sample Report $index",
                        xAxis = ReportXAxis.DAY,
                        period = ReportPeriodOption.LAST_WEEK.period,
                        series = List(3) { seriesIndex ->
                            ReportSeries(
                                reportSeriesTitle = "Series ${seriesIndex + 1}",
                                reportSeriesUid = seriesIndex + 1,
                                reportSeriesYAxis = when (seriesIndex % 3) {
                                    0 -> ReportSeriesYAxis.TOTAL_DURATION
                                    1 -> ReportSeriesYAxis.AVERAGE_DURATION
                                    else -> ReportSeriesYAxis.NUMBER_SESSIONS
                                },
                                reportSeriesVisualType = ReportSeriesVisualType.BAR_CHART,
                                reportSeriesSubGroup = ReportXAxis.DAY
                            )
                        }
                    )
                ),
                reportIsTemplate = index % 3 == 0,
                reportLastModTime = System.currentTimeMillis() - (index * 86400000L),
                reportOwnerPersonUid = 1L
            )
        }

        return _root_ide_package_.app.cash.paging.PagingSourceLoadResultPage(
            data = dummyReports,
            prevKey = null,
            nextKey = null
        )
    }

    override fun getRefreshKey(state: PagingState<Int, Report>): Int? = null
}

class EmptyPagingSource<Key : Any, Value : Any> : PagingSource<Key, Value>() {
    // TODO: Consider if this is needed in production or just for testing
    override fun getRefreshKey(state: PagingState<Key, Value>): Key? = null

    override suspend fun load(params: PagingSourceLoadParams<Key>): PagingSourceLoadResult<Key, Value> {
        return _root_ide_package_.app.cash.paging.PagingSourceLoadResultPage<Key, Value>(
            emptyList(),
            null,
            null
        ) as PagingSourceLoadResult<Key, Value>
    }
}