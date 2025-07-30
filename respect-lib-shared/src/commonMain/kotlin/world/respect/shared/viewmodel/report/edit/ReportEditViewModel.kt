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
import world.respect.shared.domain.report.model.RelativeRangeReportPeriod
import world.respect.shared.domain.report.model.ReportFilter
import world.respect.shared.domain.report.model.ReportOptions
import world.respect.shared.domain.report.model.ReportSeries
import world.respect.shared.domain.report.model.ReportSeriesVisualType
import world.respect.shared.domain.report.model.ReportSeriesYAxis
import world.respect.shared.ext.replace
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.add_a_new_report
import world.respect.shared.generated.resources.done
import world.respect.shared.generated.resources.edit_report
import world.respect.shared.generated.resources.field_required_prompt
import world.respect.shared.generated.resources.quantity_must_be_at_least_1
import world.respect.shared.generated.resources.indicator
import world.respect.shared.generated.resources.series
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.ReportDetail
import world.respect.shared.navigation.ReportEdit
import world.respect.shared.navigation.ReportEditFilter
import world.respect.shared.navigation.ReportIndictorEdit
import world.respect.shared.resources.StringResourceUiText
import world.respect.shared.resources.UiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.ActionBarButtonUiState
import world.respect.shared.viewmodel.app.appstate.AppUiState
import world.respect.shared.viewmodel.app.appstate.FabUiState
import world.respect.shared.viewmodel.app.appstate.LoadingUiState

data class ReportEditUiState(
    val reportOptions: ReportOptions = ReportOptions(),
    val reportTitleError: UiText? = null,
    val xAxisError: UiText? = null,
    val seriesTitleErrors: Map<Int, UiText> = emptyMap(),
    val yAxisErrors: Map<Int, UiText> = emptyMap(),
    val subGroupError: UiText? = null,
    val chartTypeError: Map<Int, UiText> = emptyMap(),
    val timeRangeError: UiText? = null,
    val quantityError: UiText? = null,
    val submitted: Boolean = false,
    val hasSingleSeries: Boolean = true,
)

class ReportEditViewModel(
    savedStateHandle: SavedStateHandle,
    private val respectReportDataSource: RespectReportDataSource
) : RespectViewModel(savedStateHandle) {

    private val route: ReportEdit = savedStateHandle.toRoute()
    private val entityUid: Long = route.reportUid

    private val _uiState: MutableStateFlow<ReportEditUiState> =
        MutableStateFlow(ReportEditUiState())
    val uiState: Flow<ReportEditUiState> = _uiState.asStateFlow()


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
                    title = title,
                    hideBottomNavigation = false
                )
            }

            _appUiState.update { prev ->
                prev.copy(
                    actionBarButtonState = ActionBarButtonUiState(
                        visible = true,
                        text = getString(resource = Res.string.done),
                        onClick = this@ReportEditViewModel::onClickSave
                    ),
                    userAccountIconVisible = false,
                    navigationVisible = true,
                    fabState = FabUiState(
                        text = getString(resource = Res.string.indicator),
                        icon = FabUiState.FabIcon.ADD,
                        onClick = {this@ReportEditViewModel.addIndictor()},
                        visible = true
                    )
                )
            }

            try {
                val reportFlow = respectReportDataSource.getReportAsFlow(entityUid.toString())
                launch {
                    reportFlow
                        .collect { reportState ->
                            when (reportState) {
                                is DataReadyState -> {
                                    val report = reportState.data
                                    val optionsJson = report.reportOptions

                                    val parsedOptions = try {
                                        Json.decodeFromString(
                                            ReportOptions.serializer(),
                                            optionsJson.trim()
                                        )
                                    } catch (e: Exception) {
                                        println("ERROR: JSON parsing failed: ${e.message}\n${e.stackTraceToString()}")
                                        throw IllegalArgumentException("Invalid report options format: ${e.message}")
                                    }
                                    if (entityUid == 0L) {
                                        val mockedReport = ReportOptions(
                                            title = "",
                                            series = listOf(
                                                ReportSeries(
                                                    reportSeriesUid = 1,
                                                    reportSeriesVisualType = ReportSeriesVisualType.BAR_CHART,
                                                    reportSeriesSubGroup = null,
                                                    reportSeriesYAxis = ReportSeriesYAxis.TOTAL_DURATION,
                                                    reportSeriesFilters = emptyList()
                                                )
                                            )
                                        )
                                        _uiState.update { prev ->
                                            prev.copy(
                                                reportOptions = mockedReport
                                            )
                                        }
                                    } else {
                                        _uiState.update { currentState ->
                                            currentState.copy(
                                                reportOptions = parsedOptions,
                                            )
                                        }
                                    }
                                }

                                else -> {
                                    val mockedReport = ReportOptions(
                                        series = listOf(
                                            ReportSeries(
                                                reportSeriesUid = 1,
                                                reportSeriesVisualType = ReportSeriesVisualType.BAR_CHART,
                                                reportSeriesSubGroup = null,
                                                reportSeriesYAxis = ReportSeriesYAxis.TOTAL_DURATION,
                                                reportSeriesFilters = emptyList()
                                            )
                                        )
                                    )
                                    _uiState.update { prev ->
                                        prev.copy(
                                            reportOptions = mockedReport
                                        )
                                    }
                                }
                            }
                        }
                }
            } catch (e: Exception) {
                println("Exception $e")
            }
        }
    }

    fun addIndictor(){
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                ReportIndictorEdit
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
                    reportOptions = Json.encodeToString(newState.reportOptions),
                )

                if (entityUid == 0L) {
                    respectReportDataSource.putReport(report)
                } else {
//                    respectReportDataSource.updateReport(report)
                }

                _navCommandFlow.tryEmit(
                    NavCommand.Navigate(ReportDetail.create(entityUid))
                )
            } catch (e: Exception) {
                println("Error updating report options: ${e.message}")
            } finally {
                loadingState = LoadingUiState.NOT_LOADING
            }
        }
    }

    fun onEntityChanged(newOptions: ReportOptions) {
        viewModelScope.launch {
            _uiState.update { currentState ->
                val baseUpdate = currentState.copy(
                    reportOptions = newOptions,
                    hasSingleSeries = newOptions.series.size == 1
                )

                if (!baseUpdate.submitted) baseUpdate
                else validateCurrentState().copy(
                    reportOptions = newOptions,
                    hasSingleSeries = newOptions.series.size == 1
                )
            }
        }
    }

    private fun validateCurrentState(): ReportEditUiState {
        val currentReport = _uiState.value.reportOptions
        val requiredFieldMessage = StringResourceUiText(resource = Res.string.field_required_prompt)

        val quantityError = when (val timeRange = currentReport.period) {
            is RelativeRangeReportPeriod -> {
                if (timeRange.rangeQuantity < 1) StringResourceUiText(resource = Res.string.quantity_must_be_at_least_1) else null
            }

            else -> null
        }

        val seriesTitleErrors = currentReport.series.associate { series ->
            series.reportSeriesUid to if (series.reportSeriesTitle.isEmpty()) requiredFieldMessage else null
        }.filterValues { it != null }
            .mapValues { it.value as UiText }

        val yAxisErrors = currentReport.series.associate { series ->
            series.reportSeriesUid to if (series.reportSeriesYAxis == null) requiredFieldMessage else null
        }.filterValues { it != null }
            .mapValues { it.value as UiText }

        val chartTypeErrors = currentReport.series.associate { series ->
            series.reportSeriesUid to if (series.reportSeriesVisualType == null) requiredFieldMessage else null
        }.filterValues { it != null }
            .mapValues { it.value as UiText }

        return ReportEditUiState(
            submitted = true,
            reportOptions = currentReport,
            reportTitleError = if (currentReport.title.isNullOrEmpty()) requiredFieldMessage else null,
            xAxisError = if (currentReport.xAxis == null) requiredFieldMessage else null,
            timeRangeError = if (currentReport.period == null) requiredFieldMessage else null,
            quantityError = quantityError,
            seriesTitleErrors = seriesTitleErrors,
            yAxisErrors = yAxisErrors,
            chartTypeError = chartTypeErrors,
            hasSingleSeries = currentReport.series.size == 1
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
                prev.copy(
                    reportOptions = prev.reportOptions.copy(
                        series = prev.reportOptions.series + ReportSeries(
                            reportSeriesUid = newUid,
                            reportSeriesTitle = getString(resource = Res.string.series) + newUid, // TODO: Use string resource
                            reportSeriesVisualType = ReportSeriesVisualType.BAR_CHART, // Default type
                            reportSeriesSubGroup = null,
                            reportSeriesYAxis = ReportSeriesYAxis.TOTAL_DURATION // Default Y-axis
                        ),
                    ),
                    hasSingleSeries = false
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
                ),
                hasSingleSeries = updatedSeriesList.size == 1
            )
        }
    }

    fun onAddFilter(seriesId: Int = 0) {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                ReportEditFilter
            )
        )
    }
    private fun onFilterChanged(newFilter: ReportFilter, seriesId: Int) {
        _uiState.update { prevState ->
            val updatedSeries = prevState.reportOptions.series.map { series ->
                if (series.reportSeriesUid == seriesId) {
                    val currentFilters = series.reportSeriesFilters.orEmpty().toMutableList()
                    // Check if the filter already exists by UID
                    val existingIndex = currentFilters.indexOfFirst {
                        it.reportFilterUid == newFilter.reportFilterUid
                    }
                    if (existingIndex != -1) {
                        // Replace existing filter
                        currentFilters[existingIndex] = newFilter
                    } else {
                        // Append new filter
                        currentFilters.add(newFilter)
                    }
                    series.copy(reportSeriesFilters = currentFilters)
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

    fun ReportEditUiState.hasErrors(): Boolean {
        if (!submitted) return false
        return reportTitleError != null ||
                xAxisError != null ||
                seriesTitleErrors.isNotEmpty() ||
                yAxisErrors.isNotEmpty() ||
                subGroupError != null ||
                chartTypeError.isNotEmpty() ||
                timeRangeError != null ||
                quantityError != null
    }
}