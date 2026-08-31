package world.respect.shared.viewmodel.report.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.UidNumberMapper
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.DataLoadingState
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.lib.dataloadstate.ext.map
import world.respect.lib.xapi.OpenEelXapiConstants
import world.respect.lib.xapi.OpenEelXapiConstants.CATEGORY_REPORT_QUERY_RECIPE
import world.respect.lib.xapi.ext.decodeFromExtensionOrNull
import world.respect.lib.xapi.ext.distinctByMostRecentTimestampForActivityId
import world.respect.lib.xapi.ext.objectActivityOrNull
import world.respect.lib.xapi.extensions.reportoptions.ReportOptions
import world.respect.lib.xapi.model.XapiStatement
import world.respect.lib.xapi.model.XapiStatementRef
import world.respect.lib.xapi.model.XapiVerb
import world.respect.lib.xapi.resources.XapiStatementsResource.GetStatementParams
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.report.formatter.CreateGraphFormatterUseCase
import world.respect.shared.domain.report.formatter.GraphFormatter
import world.respect.datalayer.db.school.domain.report.query.RunReportUseCase
import world.respect.shared.domain.xapi.asRunReportRequest
import world.respect.shared.domain.xapi.toStatementReportRows
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.report
import world.respect.shared.generated.resources.reports
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.ReportDetail
import world.respect.shared.navigation.ReportTemplateList
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.FabUiState
import kotlin.time.Instant

data class ReportEntry(
    val request: XapiStatement,
    val latestResponse: XapiStatement?,
    val reportResult: RunReportUseCase.RunReportResult? = null,
    val xAxisFormatter: GraphFormatter<String>? = null,
    val yAxisFormatter: GraphFormatter<Double>? = null,
)

data class ReportListUiState(
    val reportList: DataLoadState<List<ReportEntry>> = DataLoadingState(),
    val reportOptionsMap: Map<String, ReportOptions> = emptyMap(),
    val activeUserPersonUid: Long = 0L,
)


class ReportListViewModel(
    savedStateHandle: SavedStateHandle,
    private val createGraphFormatterUseCase: CreateGraphFormatterUseCase,
    private val json: Json,
    private val accountManager: RespectAccountManager,
    private val uidNumberMapper: UidNumberMapper
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()
    private val _uiState = MutableStateFlow(ReportListUiState())
    val uiState: Flow<ReportListUiState> = _uiState.asStateFlow()
    private val schoolDataSource: SchoolDataSource by inject()
    private val runReportUseCase: RunReportUseCase by inject()

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
                    ),
                    showBackButton = false,
                )
            }

            val queryRequestStatementsFlow = schoolDataSource.xapiResource.statements.getAsFlow(
                listParams = GetStatementParams(
                    activity = CATEGORY_REPORT_QUERY_RECIPE,
                    relatedActivities = true,
                    verb = XapiVerb.ID_REPORT_QUERY_REQUEST,
                ),
                dataLoadParams = DataLoadParams(),
            )

            val queryResponseStatementsFlow = schoolDataSource.xapiResource.statements.getAsFlow(
                listParams = GetStatementParams(
                    verb = XapiVerb.ID_REPORT_QUERY_RESPONSE,
                ),
                dataLoadParams = DataLoadParams(),
            )

            combine(
                queryRequestStatementsFlow,
                queryResponseStatementsFlow,
                accountManager.selectedAccountAndPersonFlow
            ) { requestsState, responsesState, sessionAndPerson ->
                val requests = requestsState.dataOrNull()?.statements ?: emptyList()
                val responses = responsesState.dataOrNull()?.statements ?: emptyList()
                val activeUserPersonUid = sessionAndPerson?.person?.guid?.let { uidNumberMapper(it) } ?: 0L

                val responsesByRequestId = responses
                    .filter { it.`object` is XapiStatementRef }
                    .groupBy { (it.`object` as XapiStatementRef).id }

                val reportEntries = requests
                    .distinctByMostRecentTimestampForActivityId()
                    .map { request ->
                        val requestId = request.id?.toString()
                        val latestResponse = responsesByRequestId[requestId]
                            ?.maxByOrNull { it.timestamp ?: it.stored ?: Instant.DISTANT_PAST }

                        val reportResult = latestResponse?.let { response ->
                            val reportOptions = request.objectActivityOrNull()?.definition?.decodeFromExtensionOrNull(
                                json = json,
                                extensionIri = OpenEelXapiConstants.EXTENSION_REPORT_OPTIONS,
                                deserializer = ReportOptions.serializer()
                            ) ?: ReportOptions()

                            val results = reportOptions.series.indices.map { index ->
                                response.toStatementReportRows(index, json)
                            }

                            RunReportUseCase.RunReportResult(
                                timestamp = response.timestamp?.toEpochMilliseconds() ?: 0L,
                                request = request.asRunReportRequest(
                                    json = json,
                                    accountPersonUid = activeUserPersonUid,
                                    timeZone = TimeZone.currentSystemDefault()
                                ),
                                results = results
                            )
                        }

                        val xAxisFormatter = reportResult?.let {
                            createGraphFormatterUseCase(
                                reportResult = it,
                                options = CreateGraphFormatterUseCase.FormatterOptions(
                                    paramType = String::class,
                                    axis = CreateGraphFormatterUseCase.FormatterOptions.Axis.X_AXIS_VALUES
                                )
                            )
                        }

                        val yAxisFormatter = reportResult?.let {
                            createGraphFormatterUseCase(
                                reportResult = it,
                                options = CreateGraphFormatterUseCase.FormatterOptions(
                                    paramType = Double::class,
                                    axis = CreateGraphFormatterUseCase.FormatterOptions.Axis.Y_AXIS_VALUES
                                )
                            )
                        }

                        ReportEntry(request, latestResponse, reportResult, xAxisFormatter, yAxisFormatter)
                    }

                val optionsMap = reportEntries.associate { entry ->
                    val stmt = entry.request
                    val options = stmt.objectActivityOrNull()?.definition?.decodeFromExtensionOrNull(
                        json = json,
                        extensionIri = OpenEelXapiConstants.EXTENSION_REPORT_OPTIONS,
                        deserializer = ReportOptions.serializer()
                    ) ?: ReportOptions()
                    stmt.id.toString() to options
                }

                _uiState.update { state ->
                    state.copy(
                        reportList = requestsState.map { reportEntries },
                        reportOptionsMap = optionsMap,
                        activeUserPersonUid = activeUserPersonUid
                    )
                }
            }.collect {}
        }
    }

    fun onClickAdd() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                ReportTemplateList
            )
        )
    }

    fun onClickEntry(entry: ReportEntry) {
        val activityId = entry.request.objectActivityOrNull()?.id ?: return
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                ReportDetail(activityId)
            )
        )
    }

    fun onRemoveReport(entry: ReportEntry) {
        val statement = entry.request
        val statementId = statement.id ?: return
        launchWithLoadingIndicator {
            val actor = accountManager.selectedAccountAndPersonFlow.first()?.xapiAgent ?: return@launchWithLoadingIndicator
            val voidingStatement = XapiStatement(
                actor = actor,
                verb = XapiVerb(id = XapiVerb.ID_VOIDED),
                `object` = XapiStatementRef(id = statementId.toString())
            )
            schoolDataSource.xapiResource.statements.post(listOf(voidingStatement))
        }
    }
}