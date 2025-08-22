package world.respect.shared.viewmodel.report.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import app.cash.paging.PagingSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.serialization.json.Json
import world.respect.datalayer.respect.EmptyPagingSource
import world.respect.datalayer.respect.RespectReportDataSource
import world.respect.datalayer.respect.model.RespectReport
import world.respect.shared.domain.report.formatter.CreateGraphFormatterUseCase
import world.respect.datalayer.realm.model.report.ReportOptions
import world.respect.shared.domain.report.model.RunReportResultAndFormatters
import world.respect.shared.domain.report.query.RunReportUseCase
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.select_template
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.ReportEdit
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
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
    private val respectReportDataSource: RespectReportDataSource,
    private val json: Json
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
                    title = Res.string.select_template.asUiText(),
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
    fun runReport(report: RespectReport): Flow<RunReportResultAndFormatters> {
        if (report.reportId == "0") {
            return flow {
                emit(
                    RunReportResultAndFormatters(
                        reportResult = RunReportUseCase.RunReportResult(
                            timestamp = Clock.System.now().toEpochMilliseconds(),
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
            }
        } else {
            val reportOptions = json.decodeFromString<ReportOptions>(
                ReportOptions.serializer(),
                report.reportOptions
            )

            val request = RunReportUseCase.RunReportRequest(
                reportUid = report.reportId.toLong(),
                reportOptions = reportOptions,
                accountPersonUid = activeUserPersonUid,
                timeZone = TimeZone.currentSystemDefault()
            )

            return runReportUseCase(request).map { reportResult ->
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

                RunReportResultAndFormatters(
                    reportResult = reportResult,
                    xAxisFormatter = xAxisFormatter,
                    yAxisFormatter = yAxisFormatter
                )
            }
        }
    }

    fun onTemplateSelected(template: RespectReport) {
        if (template.reportId == "0") { // blank template
            _navCommandFlow.tryEmit(
                NavCommand.Navigate(
                    ReportEdit(0L)
                )
            )
        } else {
            _navCommandFlow.tryEmit(
                NavCommand.Navigate(
                    ReportEdit(template.reportId.toLong())
                )
            )
        }
    }
}