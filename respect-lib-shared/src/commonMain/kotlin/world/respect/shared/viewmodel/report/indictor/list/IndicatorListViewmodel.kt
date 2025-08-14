package world.respect.shared.viewmodel.report.indictor.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import world.respect.datalayer.respect.RespectReportDataSource
import world.respect.datalayer.respect.model.Indicator
import world.respect.shared.domain.report.model.DefaultIndicators
import world.respect.shared.domain.report.query.RunReportUseCase
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.indicator
import world.respect.shared.generated.resources.indicators
import world.respect.shared.navigation.IndicatorDetail
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.IndictorEdit
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.FabUiState
import world.respect.shared.viewmodel.app.appstate.LoadingUiState.Companion.NOT_LOADING

data class IndicatorListUiState(
    val indicators: List<Indicator> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class IndicatorListViewModel(
    savedStateHandle: SavedStateHandle,
    private val respectReportDataSource: RespectReportDataSource,
    private val runReportUseCase: RunReportUseCase
) : RespectViewModel(savedStateHandle) {

    private val _uiState = MutableStateFlow(IndicatorListUiState())
    val uiState = _uiState.asStateFlow()

    // Combine default indicators with custom ones from database
    private val indicatorsFlow: Flow<List<Indicator>> =
        respectReportDataSource.getAllIndicators()
            .map { DefaultIndicators.list + it }

    init {
        viewModelScope.launch {
            _appUiState.update { prev ->
                prev.copy(
                    navigationVisible = true,
                    title = getString(resource = Res.string.indicators),
                    fabState = FabUiState(
                        text = getString(resource = Res.string.indicator),
                        icon = FabUiState.FabIcon.ADD,
                        onClick = { this@IndicatorListViewModel.onClickAdd() },
                        visible = true
                    )
                )
            }

            loadIndicators()
        }
    }

    private fun loadIndicators() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                indicatorsFlow.collect { indicators ->
                    _uiState.update {
                        it.copy(
                            indicators = indicators,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Failed to load indicators"
                    )
                }
            } finally {
                _appUiState.update { it.copy(loadingState = NOT_LOADING) }
            }
        }
    }

    fun addIndicator(indicator: Indicator) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                respectReportDataSource.saveIndicator(indicator)
                // State will update automatically through the flow
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to add indicator: ${e.message}"
                    )
                }
            }
        }
    }

    fun onClickAdd() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                IndictorEdit.create()
            )
        )
    }


    fun onIndicatorSelected(indicator: Indicator) {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                IndicatorDetail.create(
                    indicator.indicatorId
                )
            )
        )
    }
}