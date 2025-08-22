package world.respect.shared.viewmodel.report.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.getString
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.respect.RespectReportDataSource
import world.respect.datalayer.respect.model.RespectReport
import world.respect.libutil.ext.replaceOrAppend
import world.respect.datalayer.realm.model.report.DefaultIndicators
import world.respect.datalayer.realm.model.report.ReportFilter
import world.respect.datalayer.realm.model.report.ReportOptions
import world.respect.datalayer.realm.model.report.ReportSeries
import world.respect.datalayer.realm.model.report.ReportSeriesVisualType
import world.respect.shared.ext.replace
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.add_a_new_report
import world.respect.shared.generated.resources.done
import world.respect.shared.generated.resources.edit_report
import world.respect.shared.generated.resources.field_required_prompt
import world.respect.shared.generated.resources.series
import world.respect.shared.navigation.IndicatorList
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.NavResultReturner
import world.respect.shared.navigation.ReportDetail
import world.respect.shared.navigation.ReportEdit
import world.respect.shared.navigation.ReportEditFilter
import world.respect.shared.resources.StringResourceUiText
import world.respect.shared.resources.UiText
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.ActionBarButtonUiState
import world.respect.shared.viewmodel.app.appstate.AppUiState
import world.respect.shared.viewmodel.app.appstate.LoadingUiState

data class ReportEditUiState(
    val reportOptions: ReportOptions = ReportOptions(),
    val reportTitleError: UiText? = null,
    val submitted: Boolean = false
) {
    val hasSingleSeries: Boolean
        get() = reportOptions.series.size == 1
}

class ReportEditViewModel(
    savedStateHandle: SavedStateHandle,
    private val respectReportDataSource: RespectReportDataSource,
    private val json: Json,
    private val navResultReturner: NavResultReturner
) : RespectViewModel(savedStateHandle) {

    private val route: ReportEdit = savedStateHandle.toRoute()
    private val entityUid: Long = route.reportUid

    private val _uiState: MutableStateFlow<ReportEditUiState> =
        MutableStateFlow(ReportEditUiState())
    val uiState: Flow<ReportEditUiState> = _uiState.asStateFlow()
    private var nextTempFilterUid = -1


    init {
        viewModelScope.launch {
            loadingState = LoadingUiState.INDETERMINATE
            val title = if (entityUid == 0L) {
                getString(resource = Res.string.add_a_new_report)
            } else {
                getString(resource = Res.string.edit_report)
            }

            _appUiState.update {
                AppUiState(
                    title = title.asUiText(), hideBottomNavigation = false
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

        viewModelScope.launch {
            if (entityUid == 0L) {
                val newReport = ReportOptions(
                    title = "",
                    series = listOf(
                        ReportSeries()
                    )
                )
                _uiState.update { prev ->
                    prev.copy(
                        reportOptions = newReport
                    )
                }
            } else {
                val reportFlow = respectReportDataSource.getReportAsFlow(entityUid.toString())
                reportFlow.collect { reportState ->
                    when (reportState) {
                        is DataReadyState -> {
                            val report = reportState.data
                            val optionsJson = report.reportOptions

                            val parsedOptions = try {
                                json.decodeFromString(
                                    ReportOptions.serializer(), optionsJson.trim()
                                )
                            } catch (e: Exception) {
                                println("ERROR: JSON parsing failed: ${e.message}\n${e.stackTraceToString()}")
                                throw IllegalArgumentException("Invalid report options format: ${e.message}")
                            }

                            _uiState.update { currentState ->
                                currentState.copy(
                                    reportOptions = parsedOptions,
                                )
                            }
                        }

                        else -> {}
                    }
                }
            }
        }


        viewModelScope.launch {
            navResultReturner.filteredResultFlowForKey(REPORT_FILTER_RESULT).collect { result ->
                val filter = result.result as? ReportFilter
                filter?.let {
                    onFilterChanged(filter, filter.reportFilterSeriesUid.toInt())
                }
            }
        }
    }

    fun onClickManageIndicator() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                IndicatorList
            )
        )
    }

    fun onClickSave() {
        viewModelScope.launch {
            loadingState = LoadingUiState.INDETERMINATE
            val newState = validateCurrentState()
            _uiState.value = newState

            if (newState.hasErrors() || newState.reportOptions.series.isEmpty()) {
                loadingState = LoadingUiState.NOT_LOADING
                return@launch
            }

            try {
                val report = RespectReport(
                    reportId = entityUid.toString(),
                    title = newState.reportOptions.title,
                    reportOptions = json.encodeToString(newState.reportOptions),
                )

                if (entityUid == 0L) {
                    respectReportDataSource.putReport(report)
                } else {
//                    respectReportDataSource.updateReport(report)
                    //TODO
                }

                _navCommandFlow.tryEmit(
                    NavCommand.Navigate(ReportDetail(entityUid))
                )

            } catch (e: Exception) {
                println("Error updating report options: ${e.message}")
            }
        }
    }

    fun onEntityChanged(newOptions: ReportOptions) {
        viewModelScope.launch {
            _uiState.update { currentState ->
                val baseUpdate = currentState.copy(
                    reportOptions = newOptions
                )

                if (!baseUpdate.submitted) baseUpdate
                else validateCurrentState().copy(
                    reportOptions = newOptions
                )
            }
        }
    }

    private fun validateCurrentState(): ReportEditUiState {
        val currentReport = _uiState.value.reportOptions
        val requiredFieldMessage = StringResourceUiText(resource = Res.string.field_required_prompt)
        return ReportEditUiState(
            submitted = true,
            reportOptions = currentReport,
            reportTitleError = if (currentReport.title.isEmpty()) requiredFieldMessage else null,
        )
    }

    fun onSeriesChanged(updatedSeries: ReportSeries) {
        onEntityChanged(
            _uiState.value.reportOptions.let { reportOptions ->
                reportOptions.copy(
                    series = reportOptions.series.replace(updatedSeries) {
                        it.reportSeriesUid == updatedSeries.reportSeriesUid
                    }
                )
            }
        )
    }

    fun onAddSeries() {
        viewModelScope.launch {
            _uiState.update { prev ->
                val newUid = (prev.reportOptions.series.maxOfOrNull { it.reportSeriesUid } ?: 0) + 1

                // Determine the required type based on existing series
                val requiredType = prev.reportOptions.series.firstOrNull()?.reportSeriesYAxis?.type

                // Find a default indicator that matches the required type (or first available if no type restriction)
                val defaultIndicator = if (requiredType != null) {
                    DefaultIndicators.list.firstOrNull { it.type == requiredType }
                        ?: DefaultIndicators.list.first()
                } else {
                    DefaultIndicators.list.first()
                }

                prev.copy(
                    reportOptions = prev.reportOptions.copy(
                        series = prev.reportOptions.series + ReportSeries(
                            reportSeriesUid = newUid,
                            reportSeriesTitle = getString(resource = Res.string.series) + newUid,
                            reportSeriesVisualType = ReportSeriesVisualType.BAR_CHART,
                            reportSeriesSubGroup = null,
                            reportSeriesYAxis = defaultIndicator // Use the type-matched default
                        ),
                    )
                )
            }
        }
    }

    fun onRemoveSeries(seriesId: Int) {
        _uiState.update { prev ->
            val updatedSeriesList =
                prev.reportOptions.series.filterNot { it.reportSeriesUid == seriesId }
            prev.copy(
                reportOptions = prev.reportOptions.copy(
                    series = updatedSeriesList
                )
            )
        }
    }

    fun onAddFilter(seriesId: Int) {
        val tempFilterUid = nextTempFilterUid--
        val newFilter = ReportFilter(
            reportFilterUid = tempFilterUid,
            reportFilterSeriesUid = seriesId
        )

        _navCommandFlow.tryEmit(
            NavCommand.Navigate(ReportEditFilter.create(entityUid, newFilter))
        )
    }

    fun onEditFilter(reportFilter: ReportFilter) {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(ReportEditFilter.create(entityUid, reportFilter))
        )
    }

    private fun onFilterChanged(newFilter: ReportFilter?, seriesId: Int) {
        _uiState.update { prevState ->
            val updatedSeries = prevState.reportOptions.series.map { series ->
                if (series.reportSeriesUid == seriesId) {
                    val currentFilters = series.reportSeriesFilters.orEmpty()
                    val updatedFilters = currentFilters.replaceOrAppend(
                        element = newFilter ?: ReportFilter(),
                        replacePredicate = { it.reportFilterUid == newFilter?.reportFilterUid }
                    )
                    series.copy(reportSeriesFilters = updatedFilters)
                } else {
                    series
                }
            }

            prevState.copy(
                reportOptions = prevState.reportOptions.copy(series = updatedSeries)
            )
        }
        onEntityChanged(_uiState.value.reportOptions)
    }

    fun onRemoveFilter(index: Int, seriesId: Int) {
        _uiState.update { prev ->
            val updatedSeriesList = prev.reportOptions.series.map { series ->
                if (series.reportSeriesUid == seriesId) {
                    val updatedFilters = series.reportSeriesFilters?.toMutableList()?.apply {
                        removeAt(index)
                    }
                    series.copy(reportSeriesFilters = updatedFilters)
                } else {
                    series
                }
            }

            prev.copy(
                reportOptions = prev.reportOptions.copy(
                    series = updatedSeriesList
                )
            )
        }
    }

    fun ReportEditUiState.hasErrors(): Boolean {
        if (!submitted) return false
        return reportTitleError != null
    }

    companion object {
        const val REPORT_FILTER_RESULT = "report_filter_result"
    }
}