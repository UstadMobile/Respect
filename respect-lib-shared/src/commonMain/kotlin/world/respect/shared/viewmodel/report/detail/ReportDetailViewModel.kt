package world.respect.shared.viewmodel.report.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import org.jetbrains.compose.resources.getString
import world.respect.datalayer.db.shared.entities.Report
import world.respect.shared.domain.report.formatter.CreateGraphFormatterUseCase
import world.respect.shared.domain.report.formatter.GraphFormatter
import world.respect.shared.domain.report.model.ReportOptions
import world.respect.shared.domain.report.model.ReportSeries
import world.respect.shared.domain.report.model.ReportSeriesVisualType
import world.respect.shared.domain.report.model.ReportSeriesYAxis
import world.respect.shared.domain.report.model.ReportXAxis
import world.respect.shared.domain.report.model.StatementReportRow
import world.respect.shared.domain.report.query.RunReportUseCase
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.edit
import world.respect.shared.generated.resources.invalid_report_config
import world.respect.shared.generated.resources.invalid_report_format
import world.respect.shared.generated.resources.unknown_error
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.ReportDetail
import world.respect.shared.navigation.ReportEdit
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.FabUiState
import world.respect.shared.viewmodel.app.appstate.LoadingUiState.Companion.NOT_LOADING

data class ReportDetailUiState(
    val report: Report? = null,
    val reportResult: RunReportUseCase.RunReportResult? = null,
    val errorMessage: String? = null,
    val reportOptions2: ReportOptions = ReportOptions(),
    val xAxisFormatter: GraphFormatter<String>? = null,
    val yAxisFormatter: GraphFormatter<Double>? = null,
    val subgroupFormatter: GraphFormatter<String>? = null

)

class ReportDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val runReportUseCase: RunReportUseCase,
    private val createGraphFormatterUseCase: CreateGraphFormatterUseCase
) : RespectViewModel(savedStateHandle) {

    private val route: ReportDetail = savedStateHandle.toRoute()

    private val reportUid: Long = route.reportUid

    private val _uiState = MutableStateFlow(ReportDetailUiState())
    val uiState: Flow<ReportDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _appUiState.update { prev ->
                prev.copy(
                    fabState = FabUiState(
                        visible = true,
                        text = getString(resource = Res.string.edit),
                        icon = FabUiState.FabIcon.EDIT,
                        onClick = {
                            _navCommandFlow.tryEmit(
                                NavCommand.Navigate(
                                    ReportEdit.create(reportUid)
                                )
                            )
                        },
                    )
                )
            }

            try {
                // TODO: Replace with actual report options from database
                val mockOptions = ReportOptions(
                    title = "Weekly Session Report",
                    xAxis = ReportXAxis.YEAR,
                    series = listOf(
                        ReportSeries(
                            reportSeriesUid = 1,
                            reportSeriesTitle = "Session Duration",
                            reportSeriesYAxis = ReportSeriesYAxis.TOTAL_DURATION,
                            reportSeriesVisualType = ReportSeriesVisualType.BAR_CHART,
                            reportSeriesSubGroup = ReportXAxis.GENDER
                        ),
                        ReportSeries(
                            reportSeriesUid = 2,
                            reportSeriesTitle = "Average Duration",
                            reportSeriesYAxis = ReportSeriesYAxis.AVERAGE_DURATION,
                            reportSeriesVisualType = ReportSeriesVisualType.LINE_GRAPH,
                            reportSeriesSubGroup = ReportXAxis.GENDER
                        )
                    )
                )

                _appUiState.update { prev ->
                    prev.copy(title = mockOptions.title)
                }

                _uiState.update {
                    it.copy(reportOptions2 = mockOptions)
                }

                // TODO: Replace with actual request parameters
                val request = RunReportUseCase.RunReportRequest(
                    reportUid = 0L,
                    reportOptions = mockOptions,
                    accountPersonUid = 0L, // TODO: Get actual user ID
                    timeZone = TimeZone.currentSystemDefault()
                )

                // TODO: Replace with actual report results from database
                val mockReportResult = RunReportUseCase.RunReportResult(
                    results = listOf(
                        listOf(
                            StatementReportRow(120383.0, "2023-01-01", "2"),
                            StatementReportRow(183248.0, "2023-01-02", "1"),
                            StatementReportRow(9732.0, "2023-01-03", "1"),
                            StatementReportRow(2187324.0, "2023-01-04", "2"),
                            StatementReportRow(187423.0, "2023-01-05", "1"),
                            StatementReportRow(33033.0, "2023-01-06", "2"),
                            StatementReportRow(2362.0, "2023-01-07", "1")
                        ),
                        // Second series data
                        listOf(
                            StatementReportRow(6324.0, "2023-01-01", "1"),
                            StatementReportRow(9730.0, "2023-01-02", "1"),
                            StatementReportRow(43325.0, "2023-01-03", "2"),
                            StatementReportRow(18325.0, "2023-01-04", "1"),
                            StatementReportRow(753874.0, "2023-01-05", "2"),
                            StatementReportRow(03847.0, "2023-01-06", "2"),
                            StatementReportRow(023783.0, "2023-01-07", "2")
                        )
                    ),
                    timestamp = System.currentTimeMillis(),
                    request = request,
                    age = 0
                )

                // TODO: Consider caching formatters
                val xAxisFormatter = createGraphFormatterUseCase(
                    reportResult = mockReportResult,
                    options = CreateGraphFormatterUseCase.FormatterOptions(
                        paramType = String::class,
                        axis = CreateGraphFormatterUseCase.FormatterOptions.Axis.X_AXIS_VALUES
                    )
                )
                val subgroupFormatter = createGraphFormatterUseCase(
                    reportResult = mockReportResult,
                    options = CreateGraphFormatterUseCase.FormatterOptions(
                        paramType = String::class,
                        axis = CreateGraphFormatterUseCase.FormatterOptions.Axis.X_AXIS_VALUES,
                        forSubgroup = true
                    )
                )

                val yAxisFormatter = createGraphFormatterUseCase(
                    reportResult = mockReportResult,
                    options = CreateGraphFormatterUseCase.FormatterOptions(
                        paramType = Double::class,
                        axis = CreateGraphFormatterUseCase.FormatterOptions.Axis.Y_AXIS_VALUES
                    )
                )

                _uiState.update { prev ->
                    prev.copy(
                        reportResult = mockReportResult,
                        xAxisFormatter = xAxisFormatter,
                        yAxisFormatter = yAxisFormatter,
                        subgroupFormatter = subgroupFormatter
                    )
                }

            } catch (e: Exception) {
                val errorMsg = when (e) {
                    is IllegalArgumentException -> getString(resource = Res.string.invalid_report_format)
                    is IllegalStateException -> e.message
                        ?: getString(resource = Res.string.invalid_report_config)

                    else -> e.message ?: getString(resource = Res.string.unknown_error)
                }

                _uiState.update {
                    it.copy(errorMessage = errorMsg)
                }

            } finally {
                _appUiState.update { it.copy(loadingState = NOT_LOADING) }
            }
        }
    }
}