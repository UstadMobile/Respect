package world.respect.shared.viewmodel.report.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.school.ReportDataSource
import world.respect.datalayer.school.model.Report
import world.respect.datalayer.shared.paging.EmptyPagingSourceFactory
import world.respect.datalayer.shared.paging.IPagingSourceFactory
import world.respect.datalayer.shared.paging.PagingSourceFactoryHolder
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.report.formatter.CreateGraphFormatterUseCase
import world.respect.shared.domain.report.formatter.GraphFormatter
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
    val reportList: IPagingSourceFactory<Int, Report> = EmptyPagingSourceFactory(),
    val activeUserPersonUid: Long = 0L,
    val xAxisFormatter: GraphFormatter<String>? = null,
    val yAxisFormatter: GraphFormatter<Double>? = null
)

class ReportListViewModel(
    savedStateHandle: SavedStateHandle,
    private val runReportUseCase: RunReportUseCase,
    private val createGraphFormatterUseCase: CreateGraphFormatterUseCase,
    accountManager: RespectAccountManager
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireSelectedAccountScope()
    private val _uiState = MutableStateFlow(ReportListUiState())
    val uiState: Flow<ReportListUiState> = _uiState.asStateFlow()
    private val schoolDataSource: SchoolDataSource by inject()

    private val pagingSourceHolder = PagingSourceFactoryHolder {
        schoolDataSource.reportDataSource.listAsPagingSource(
            loadParams = DataLoadParams(),
            params = ReportDataSource.GetListParams(),
            template = false
        )
    }
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
            viewModelScope.launch {
                _uiState.update { prev ->
                    prev.copy(
                        reportList = pagingSourceHolder,
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    fun runReport(report: Report): Flow<RunReportResultAndFormatters> {
        val request = RunReportUseCase.RunReportRequest(
            reportUid = report.guid.toLong(),
            reportOptions = report.reportOptions,
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

    fun onClickEntry(entry: Report) {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                ReportDetail(entry.guid)
            )
        )
    }

    fun onRemoveReport(uid: String) {
        viewModelScope.launch {
            schoolDataSource.reportDataSource.delete(uid)
        }
    }
}