package world.respect.shared.viewmodel.report.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import app.cash.paging.PagingSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.getString
import world.respect.datalayer.respect.EmptyPagingSource
import world.respect.datalayer.respect.RespectReportDataSource
import world.respect.datalayer.respect.model.RespectReport
import world.respect.shared.domain.report.formatter.CreateGraphFormatterUseCase
import world.respect.shared.domain.report.model.ReportOptions
import world.respect.shared.domain.report.model.RunReportResultAndFormatters
import world.respect.shared.domain.report.model.StatementReportRow
import world.respect.shared.domain.report.query.RunReportUseCase
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.select_template
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.ReportEdit
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.LoadingUiState.Companion.NOT_LOADING
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

data class ReportTemplateListUiState(
    val templates: () -> PagingSource<Int, RespectReport> = { EmptyPagingSource() },
    val isLoading: Boolean = false,
    val error: String? = null,
    val activeUserPersonUid: Long = 0L,
    )

class ReportTemplateListViewModel(
    savedStateHandle: SavedStateHandle,
    private val runReportUseCase: RunReportUseCase,
    private val createGraphFormatterUseCase: CreateGraphFormatterUseCase,
    private val respectReportDataSource: RespectReportDataSource
) : RespectViewModel(savedStateHandle) {

    private val _uiState = MutableStateFlow(ReportTemplateListUiState())
    val uiState = _uiState.asStateFlow()

    private val pagingSourceFactory: () -> PagingSource<Int, RespectReport> = {
        respectReportDataSource.getTemplateReportsPagingSource()
    }
    private val activeUserPersonUid: Long = 0

    init {
        viewModelScope.launch {
            // Setup UI
            _appUiState.update { prev ->
                prev.copy(
                    navigationVisible = true,
                    title = getString(resource = Res.string.select_template),
                )
            }

            _uiState.update { prev ->
                prev.copy(
                    templates = pagingSourceFactory,
                    activeUserPersonUid = activeUserPersonUid
                )
            }
        }
    }


    @OptIn(ExperimentalTime::class)
    fun runReport(report: RespectReport): Flow<RunReportResultAndFormatters> = flow {
        try {
            if (report.reportId == "0") {
                emit(
                    RunReportResultAndFormatters(
                        reportResult = RunReportUseCase.RunReportResult(
                            timestamp = System.currentTimeMillis(),
                            request = RunReportUseCase.RunReportRequest(
                                reportUid = 0L,
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
            } else {
                val reportOptions = Json.decodeFromString<ReportOptions>(
                    ReportOptions.serializer(),
                    report.reportOptions
                )

                val request = RunReportUseCase.RunReportRequest(
                    reportUid = report.reportId.toLong(),
                    reportOptions = reportOptions,
                    accountPersonUid = activeUserPersonUid,
                    timeZone = TimeZone.currentSystemDefault()
                )

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

                val xAxisFormatter = createGraphFormatterUseCase(
                    reportResult = mockReportResult,
                    options = CreateGraphFormatterUseCase.FormatterOptions(
                        paramType = String::class,
                        axis = CreateGraphFormatterUseCase.FormatterOptions.Axis.X_AXIS_VALUES
                    )
                )

                val yAxisFormatter = createGraphFormatterUseCase(
                    reportResult = mockReportResult,
                    options = CreateGraphFormatterUseCase.FormatterOptions(
                        paramType = Double::class,
                        axis = CreateGraphFormatterUseCase.FormatterOptions.Axis.Y_AXIS_VALUES
                    )
                )

                emit(
                    RunReportResultAndFormatters(
                        reportResult = mockReportResult,
                        xAxisFormatter = xAxisFormatter,
                        yAxisFormatter = yAxisFormatter
                    )
                )
            }
        } catch (e: Exception) {
            emit(
                RunReportResultAndFormatters(
                    reportResult = RunReportUseCase.RunReportResult(
                        timestamp = Clock.System.now().toEpochMilliseconds(),
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
        } finally {
            _appUiState.update { it.copy(loadingState = NOT_LOADING) }
        }
    }
    fun getBlankTemplate(): RespectReport {
        return RespectReport(
            reportId = "0",
            title = "Blank Report",
            reportOptions = Json.encodeToString(ReportOptions.serializer(), ReportOptions()),
            reportIsTemplate = true,
            // ... other required fields ...
        )
    }
    fun onTemplateSelected(template: RespectReport) {
        if (template.reportId == "0") { // This is our blank template
            _navCommandFlow.tryEmit(
                NavCommand.Navigate(
                    ReportEdit.create(0L)
                )
            )        } else {
            _navCommandFlow.tryEmit(
                NavCommand.Navigate(
                    ReportEdit.create(template.reportId.toLong())
                )
            )
        }
    }
}