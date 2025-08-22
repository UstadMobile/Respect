package world.respect.shared.viewmodel.report.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import app.cash.paging.PagingSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.serialization.json.Json
import world.respect.datalayer.respect.EmptyPagingSource
import world.respect.datalayer.respect.RespectReportDataSource
import world.respect.datalayer.respect.model.RespectReport
import world.respect.shared.domain.report.formatter.CreateGraphFormatterUseCase
import world.respect.shared.domain.report.formatter.GraphFormatter
import world.respect.datalayer.realm.model.report.ReportOptions
import world.respect.shared.domain.report.model.RunReportResultAndFormatters
import world.respect.shared.domain.report.query.RunReportUseCase
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.report
import world.respect.shared.generated.resources.reports
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.ReportDetail
import world.respect.shared.navigation.ReportTemplateList
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.FabUiState
import kotlin.time.ExperimentalTime

data class ReportListUiState(
    val reportList: () -> PagingSource<Int, RespectReport> = { EmptyPagingSource() },
    val activeUserPersonUid: Long = 0L,
    val xAxisFormatter: GraphFormatter<String>? = null,
    val yAxisFormatter: GraphFormatter<Double>? = null
)

class ReportListViewModel(
    savedStateHandle: SavedStateHandle,
    private val runReportUseCase: RunReportUseCase,
    private val createGraphFormatterUseCase: CreateGraphFormatterUseCase,
    private val respectReportDataSource: RespectReportDataSource,
    private val json: Json
) : RespectViewModel(savedStateHandle) {

    private val _uiState = MutableStateFlow(ReportListUiState())
    val uiState: Flow<ReportListUiState> = _uiState.asStateFlow()

    private val pagingSourceFactory: () -> PagingSource<Int, RespectReport> = {
        respectReportDataSource.getReportsPagingSource()
    }
    private val activeUserPersonUid: Long = 0

    init {
        viewModelScope.launch {
            _appUiState.update { prev ->
                prev.copy(
                    navigationVisible = true,
                    title = Res.string.reports.asUiText(),
                    fabState = FabUiState(
                        text = Res.string.report.asUiText(),
                        icon = FabUiState.FabIcon.ADD,
                        onClick = { this@ReportListViewModel.onClickAdd() },
                        visible = true
                    )
                )
            }

            // Initialize paging data
            _uiState.update { prev ->
                prev.copy(
                    reportList = pagingSourceFactory,
                    activeUserPersonUid = activeUserPersonUid
                )
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    fun runReport(report: RespectReport): Flow<RunReportResultAndFormatters> {
        val reportOptions = json.decodeFromString<ReportOptions>(
            ReportOptions.serializer(),
            report.reportOptions
        )
        val request = RunReportUseCase.RunReportRequest(
            reportUid = report.reportId.toLong(),
            reportOptions = reportOptions,
            accountPersonUid = 0L, // TODO: Get actual user ID
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

    fun onClickAdd() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                ReportTemplateList
            )
        )
    }

    fun onClickEntry(entry: RespectReport) {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                ReportDetail(entry.reportId.toLong())
            )
        )
    }

    fun onRemoveReport(uid: Long) {
        // TODO Implement remove functionality
    }
}