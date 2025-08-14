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
import world.respect.datalayer.respect.model.Indicator
import world.respect.datalayer.respect.model.RespectReport
import world.respect.shared.domain.report.model.DefaultIndicators
import world.respect.shared.domain.report.model.RelativeRangeReportPeriod
import world.respect.shared.domain.report.model.ReportFilter
import world.respect.shared.domain.report.model.ReportOptions
import world.respect.shared.domain.report.model.ReportSeries
import world.respect.shared.domain.report.model.ReportSeriesVisualType
import world.respect.shared.ext.replace
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.add_a_new_report
import world.respect.shared.generated.resources.done
import world.respect.shared.generated.resources.edit_report
import world.respect.shared.generated.resources.field_required_prompt
import world.respect.shared.generated.resources.quantity_must_be_at_least_1
import world.respect.shared.generated.resources.series
import world.respect.shared.navigation.IndicatorList
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.ReportDetail
import world.respect.shared.navigation.ReportEdit
import world.respect.shared.navigation.ReportEditFilter
import world.respect.shared.resources.StringResourceUiText
import world.respect.shared.resources.UiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.ActionBarButtonUiState
import world.respect.shared.viewmodel.app.appstate.AppUiState
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
                    title = title, hideBottomNavigation = false
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
                )
            }

            try {
                val reportFlow = respectReportDataSource.getReportAsFlow(entityUid.toString())
                launch {
                    reportFlow.collect { reportState ->
                        when (reportState) {
                            is DataReadyState -> {
                                val report = reportState.data
                                val optionsJson = report.reportOptions

                                val parsedOptions = try {
                                    Json.decodeFromString(
                                        ReportOptions.serializer(), optionsJson.trim()
                                    )
                                } catch (e: Exception) {
                                    println("ERROR: JSON parsing failed: ${e.message}\n${e.stackTraceToString()}")
                                    throw IllegalArgumentException("Invalid report options format: ${e.message}")
                                }
                                if (entityUid == 0L) {
                                    val mockedReport = ReportOptions(
                                        title = "", series = listOf(
                                            ReportSeries(
                                                reportSeriesUid = 1,
                                                reportSeriesVisualType = ReportSeriesVisualType.BAR_CHART,
                                                reportSeriesSubGroup = null,
                                                reportSeriesYAxis = DefaultIndicators.list.first(),
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
                                            reportSeriesYAxis = DefaultIndicators.list.first(),
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
            viewModelScope.launch {
                route.filter?.let { filter ->
                    val seriesId = filter.reportFilterSeriesUid
                    onFilterChanged(filter, seriesId)
                }
            }
        }
    }

    fun manageIndicator() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                IndicatorList
            )
        )
    }

    private fun updateSeriesWithIndicator(indicator: Indicator) {
        _uiState.update { currentState ->
            val seriesId = 0 // TODO need to update Indicators as per series/ report
            val updatedSeries = currentState.reportOptions.series.map { series ->
                if (series.reportSeriesUid == seriesId) {
                    series.copy(reportSeriesYAxis = indicator)
                } else {
                    series
                }
            }
            currentState.copy(
                reportOptions = currentState.reportOptions.copy(series = updatedSeries)
            )
        }
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
                    //TODO
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
                    reportOptions = newOptions, hasSingleSeries = newOptions.series.size == 1
                )

                if (!baseUpdate.submitted) baseUpdate
                else validateCurrentState().copy(
                    reportOptions = newOptions, hasSingleSeries = newOptions.series.size == 1
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
        }.filterValues { it != null }.mapValues { it.value as UiText }

        val yAxisErrors = currentReport.series.associate { series ->
            series.reportSeriesUid to if (series.reportSeriesYAxis == null) requiredFieldMessage else null
        }.filterValues { it != null }.mapValues { it.value as UiText }

        val chartTypeErrors = currentReport.series.associate { series ->
            series.reportSeriesUid to if (series.reportSeriesVisualType == null) requiredFieldMessage else null
        }.filterValues { it != null }.mapValues { it.value as UiText }

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
                    })
            })
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
                    ), hasSingleSeries = false
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
                ), hasSingleSeries = updatedSeriesList.size == 1
            )
        }
    }

    fun onAddFilter(seriesId: Int) {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                ReportEditFilter.create(
                    reportUid = entityUid, seriesId = seriesId
                )
            )
        )
    }

    private fun onFilterChanged(newFilter: ReportFilter?, seriesId: Int) {
        _uiState.update { prevState ->
            val updatedSeries = prevState.reportOptions.series.map { series ->
                if (series.reportSeriesUid == seriesId) {
                    val currentFilters = series.reportSeriesFilters.orEmpty().toMutableList()
                    // Check if the filter already exists by UID
                    val existingIndex = currentFilters.indexOfFirst {
                        it.reportFilterUid == newFilter?.reportFilterUid
                    }
                    if (existingIndex != -1) {
                        // Replace existing filter
                        currentFilters[existingIndex] = newFilter ?: ReportFilter()
                    } else {
                        // Append new filter
                        currentFilters.add(newFilter ?: ReportFilter())
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
        return reportTitleError != null || xAxisError != null || seriesTitleErrors.isNotEmpty() || yAxisErrors.isNotEmpty() || subGroupError != null || chartTypeError.isNotEmpty() || timeRangeError != null || quantityError != null
    }
}