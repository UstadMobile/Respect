package world.respect.shared.viewmodel.report.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.getString
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.school.domain.report.query.GenerateReportQueriesUseCase
import world.respect.datalayer.db.school.domain.report.query.RunReportUseCase
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.lib.dataloadstate.ext.firstOrNotLoaded
import world.respect.lib.dataloadstate.ext.map
import world.respect.libutil.ext.appendEndpointSegments
import world.respect.lib.xapi.OpenEelXapiConstants
import world.respect.lib.xapi.ext.decodeFromExtensionOrNull
import world.respect.lib.xapi.ext.encodeWithExtension
import world.respect.lib.xapi.ext.mostRecentByTimestampOrNull
import world.respect.lib.xapi.ext.objectActivityOrNull
import world.respect.lib.xapi.extensions.reportoptions.DefaultIndicators
import world.respect.lib.xapi.extensions.reportoptions.Indicator
import world.respect.lib.xapi.extensions.reportoptions.ReportFilter
import world.respect.lib.xapi.extensions.reportoptions.ReportOptions
import world.respect.lib.xapi.extensions.reportoptions.ReportSeries
import world.respect.lib.xapi.extensions.reportoptions.ReportSeriesVisualType
import world.respect.lib.xapi.model.XapiActivityDefinition
import world.respect.lib.xapi.model.XapiStatement
import world.respect.lib.xapi.model.XapiVerb
import world.respect.lib.xapi.resources.XapiStatementsResource.GetStatementParams
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.xapi.createBlankReportStatement
import world.respect.shared.domain.xapi.fillSqlParameters
import world.respect.shared.domain.xapi.withReportQueries
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.add_a_new_report
import world.respect.shared.generated.resources.done
import world.respect.shared.generated.resources.edit_report
import world.respect.shared.generated.resources.field_required_prompt
import world.respect.shared.generated.resources.series
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.NavResultReturner
import world.respect.shared.navigation.ReportDetail
import world.respect.shared.navigation.ReportEdit
import world.respect.shared.navigation.ReportEditFilter
import world.respect.shared.resources.StringResourceUiText
import world.respect.shared.resources.UiText
import world.respect.shared.util.LaunchDebouncer
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.ActionBarButtonUiState
import world.respect.shared.viewmodel.app.appstate.AppUiState
import world.respect.shared.viewmodel.app.appstate.LoadingUiState
import world.respect.shared.viewmodel.app.appstate.Snack
import world.respect.shared.viewmodel.app.appstate.SnackBarDispatcher
import kotlin.time.Clock
import kotlin.uuid.Uuid

data class ReportEditUiState(
    val reportOptions: ReportOptions = ReportOptions(),
    val submitted: Boolean = false
) {
    val hasSingleSeries: Boolean
        get() = reportOptions.series.size == 1

    val isTitleError: Boolean
        get() = submitted && reportOptions.title.isBlank()

    val reportTitleError: UiText?
        get() = if (isTitleError) StringResourceUiText(resource = Res.string.field_required_prompt) else null
}

class ReportEditViewModel(
    savedStateHandle: SavedStateHandle,
    private val accountManager: RespectAccountManager,
    private val json: Json,
    private val navResultReturner: NavResultReturner,
    private val snackBarDispatcher: SnackBarDispatcher
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()

    private val schoolDataSource: SchoolDataSource by inject()
    private val generateReportQueriesUseCase: GenerateReportQueriesUseCase by inject()
    private val uidNumberMapper: UidNumberMapper by inject()
    private val route: ReportEdit = savedStateHandle.toRoute()

    private val schoolUrl = accountManager.requireActiveSchoolUrl()

    private val entityUid = route.reportActivityUid ?: run {
        schoolUrl.appendEndpointSegments(REPORTS, Uuid.random().toString()).toString()
    }

    private val _uiState: MutableStateFlow<ReportEditUiState> =
        MutableStateFlow(ReportEditUiState())
    val uiState: Flow<ReportEditUiState> = _uiState.asStateFlow()
    private val debouncer = LaunchDebouncer(viewModelScope)
    private var baseStatement: XapiStatement? = null


    init {
        viewModelScope.launch {
            loadingState = LoadingUiState.INDETERMINATE
            val title = if (route.reportActivityUid == null) {
                getString(resource = Res.string.add_a_new_report)
            } else {
                getString(resource = Res.string.edit_report)
            }

            _appUiState.update {
                AppUiState(
                    title = title.asUiText(),
                    hideBottomNavigation = true
                )
            }

            _appUiState.update { prev ->
                prev.copy(
                    actionBarButtonState = ActionBarButtonUiState(
                        visible = true,
                        text = Res.string.done.asUiText(),
                        onClick = this@ReportEditViewModel::onClickSave
                    ),
                    userAccountIconVisible = false,
                    navigationVisible = true,
                )
            }
        }

        launchWithLoadingIndicator(
            onShowError = { snackBarDispatcher.showSnackBar(Snack(it)) }
        ) {
            if (route.reportActivityUid != null) {
                loadEntity(
                    json = json,
                    serializer = XapiStatement.serializer(),
                    loadFn = { params ->
                        schoolDataSource.xapiResource.statements.get(
                            listParams = GetStatementParams(
                                activity = route.reportActivityUid,
                                verb = XapiVerb.ID_REPORT_QUERY_REQUEST,
                            ),
                            dataLoadParams = params
                        ).map { result ->
                            result.statements.mostRecentByTimestampOrNull()?.let {
                                listOf(it)
                            } ?: emptyList()
                        }.firstOrNotLoaded()
                    },
                    uiUpdateFn = { entity ->
                        val statement = entity.dataOrNull()
                        if (statement != null) {
                            baseStatement = statement
                        }
                        _uiState.update { prev ->
                            prev.copy(
                                reportOptions = statement
                                    ?.objectActivityOrNull()?.definition?.decodeFromExtensionOrNull(
                                        json = json,
                                        extensionIri = OpenEelXapiConstants.EXTENSION_REPORT_OPTIONS,
                                        deserializer = ReportOptions.serializer()
                                    ) ?: ReportOptions(series = listOf(ReportSeries()))
                            )
                        }
                    }
                )
            } else {
                val initialOptions = ReportOptions(
                    series = listOf(
                        ReportSeries()
                    )
                )
                val actor = accountManager.selectedAccountAndPersonFlow.first()?.xapiAgent
                    ?: return@launchWithLoadingIndicator

                baseStatement = createBlankReportStatement(
                    reportActivityId = entityUid,
                    actor = actor,
                    reportOptions = initialOptions,
                    json = json
                )
                _uiState.update { prev ->
                    prev.copy(
                        reportOptions = initialOptions
                    )
                }
            }
        }

        viewModelScope.launch {
            navResultReturner.filteredResultFlowForKey(REPORT_EDIT_FILTER_RESULT)
                .collect { result ->
                    val filter = result.result as? ReportFilter
                    filter?.let {
                        onFilterChanged(it)
                    }
                }
        }
    }

    fun onClickSave() {
        _uiState.update { prev ->
            prev.copy(submitted = true)
        }

        val currentOptions = _uiState.value.reportOptions
        if (_uiState.value.isTitleError || currentOptions.series.isEmpty()) {
            return
        }

        val reportStatement = baseStatement ?: return
        launchWithLoadingIndicator {
            val sessionAndPerson = accountManager.selectedAccountAndPersonFlow.first()
            val queries = if (sessionAndPerson != null) {
                val request = RunReportUseCase.RunReportRequest(
                    reportUid = entityUid,
                    reportOptions = currentOptions,
                    accountPersonUid = uidNumberMapper(sessionAndPerson.person.guid),
                    timeZone = kotlinx.datetime.TimeZone.currentSystemDefault()
                )
                val generated = generateReportQueriesUseCase(request)
                generated.map { parts ->
                    val sql = fillSqlParameters(parts.sql, parts.params)
                    sql
                }
            } else {
                emptyList()
            }

            schoolDataSource.xapiResource.statements.post(
                listOf(
                    reportStatement.copy(
                        id = Uuid.random(),
                        timestamp = Clock.System.now(),
                    ).withReportQueries(queries, json)
                )
            )
            if (route.reportActivityUid == null) {
                _navCommandFlow.tryEmit(
                    NavCommand.Navigate(
                        ReportDetail(entityUid), popUpTo = route, popUpToInclusive = true
                    )
                )
            } else {
                _navCommandFlow.tryEmit(NavCommand.PopUp())
            }
        }
    }

    fun onEntityChanged(options: ReportOptions) {
        val currentStmt = baseStatement ?: return
        val updatedActivity = currentStmt.objectActivityOrNull()?.let { activity ->
            activity.copy(
                definition = (activity.definition ?: XapiActivityDefinition()).encodeWithExtension(
                    json = json,
                    extensionIri = OpenEelXapiConstants.EXTENSION_REPORT_OPTIONS,
                    serializer = ReportOptions.serializer(),
                    value = options
                ).copy(
                    name = mapOf("en" to options.title) // TODO NEED TO CHANGE HARDCODED LANGUAGE
                )
            )
        } ?: currentStmt.`object`

        val updatedStmt = currentStmt.copy(`object` = updatedActivity)
        baseStatement = updatedStmt

        _uiState.update { currentState ->
            currentState.copy(
                reportOptions = options,
            )
        }

        debouncer.launch(DEFAULT_SAVED_STATE_KEY) {
            savedStateHandle[DEFAULT_SAVED_STATE_KEY] =
                json.encodeToString(XapiStatement.serializer(), updatedStmt)
        }
    }

    fun onSeriesChanged(index: Int, updatedSeries: ReportSeries) {
        val currentOptions = _uiState.value.reportOptions
        val newOptions = currentOptions.copy(
            series = currentOptions.series.toMutableList().apply {
                set(index, updatedSeries)
            }
        )
        onEntityChanged(newOptions)
    }

    fun onAddSeries() {
        viewModelScope.launch {
            val currentOptions = _uiState.value.reportOptions
            val nextSeriesNum = currentOptions.series.size + 1

            // Determine the required type based on existing series
            val requiredType = currentOptions.series.firstOrNull()?.reportSeriesYAxis?.type

            // Find a default indicator that matches the required type (or first available if no type restriction)
            val defaultIndicator = if (requiredType != null) {
                DefaultIndicators.list.firstOrNull { it.type == requiredType }
                    ?: DefaultIndicators.list.first()
            } else {
                DefaultIndicators.list.first()
            }

            val newOptions = currentOptions.copy(
                series = currentOptions.series + ReportSeries(
                    reportSeriesTitle = getString(resource = Res.string.series) + nextSeriesNum,
                    reportSeriesVisualType = ReportSeriesVisualType.BAR_CHART,
                    reportSeriesYAxis = defaultIndicator
                ),
            )
            onEntityChanged(newOptions)
        }
    }

    fun onRemoveSeries(index: Int) {
        val currentOptions = _uiState.value.reportOptions
        val updatedSeriesList =
            currentOptions.series.toMutableList().apply {
                removeAt(index)
            }
        val newOptions = currentOptions.copy(
            series = updatedSeriesList
        )
        onEntityChanged(newOptions)
    }

    fun onAddFilter(seriesIndex: Int) {
        savedStateHandle[SAVED_STATE_SERIES_INDEX] = seriesIndex
        savedStateHandle[SAVED_STATE_FILTER_INDEX] = -1
        val newFilter = ReportFilter()
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(ReportEditFilter.create(newFilter))
        )
    }

    fun onEditFilter(seriesIndex: Int, filterIndex: Int, reportFilter: ReportFilter) {
        savedStateHandle[SAVED_STATE_SERIES_INDEX] = seriesIndex
        savedStateHandle[SAVED_STATE_FILTER_INDEX] = filterIndex
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(ReportEditFilter.create(reportFilter))
        )
    }

    private fun onFilterChanged(filter: ReportFilter) {
        val seriesIndex = savedStateHandle.get<Int>(SAVED_STATE_SERIES_INDEX) ?: return
        val filterIndex = savedStateHandle.get<Int>(SAVED_STATE_FILTER_INDEX) ?: -1

        val currentOptions = _uiState.value.reportOptions
        val updatedSeriesList = currentOptions.series.toMutableList()
        val series = updatedSeriesList.getOrNull(seriesIndex) ?: return

        val currentFilters = series.reportSeriesFilters.orEmpty().toMutableList()
        if (filterIndex >= 0 && filterIndex < currentFilters.size) {
            currentFilters[filterIndex] = filter
        } else {
            currentFilters.add(filter)
        }

        updatedSeriesList[seriesIndex] = series.copy(reportSeriesFilters = currentFilters)
        val newOptions = currentOptions.copy(series = updatedSeriesList)
        onEntityChanged(newOptions)
    }

    fun onRemoveFilter(seriesIndex: Int, filterIndex: Int) {
        val currentOptions = _uiState.value.reportOptions
        val updatedSeries = currentOptions.series.toMutableList()
        val series = updatedSeries.getOrNull(seriesIndex) ?: return

        val updatedFilters = series.reportSeriesFilters?.toMutableList()?.apply {
            removeAt(filterIndex)
        }
        updatedSeries[seriesIndex] = series.copy(reportSeriesFilters = updatedFilters)

        val newOptions = currentOptions.copy(
            series = updatedSeries
        )
        onEntityChanged(newOptions)
    }

    companion object {
        const val REPORT_EDIT_FILTER_RESULT = "report_filter_result"
        private const val SAVED_STATE_SERIES_INDEX = "seriesIndex"
        private const val SAVED_STATE_FILTER_INDEX = "filterIndex"
        private const val REPORTS = "reports"
    }
}