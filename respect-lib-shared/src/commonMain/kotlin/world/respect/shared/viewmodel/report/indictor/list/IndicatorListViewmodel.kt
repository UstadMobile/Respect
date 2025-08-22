package world.respect.shared.viewmodel.report.indictor.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import world.respect.datalayer.respect.RespectReportDataSource
import world.respect.datalayer.respect.model.Indicator
import world.respect.datalayer.realm.model.report.DefaultIndicators
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.indicator
import world.respect.shared.generated.resources.indicators
import world.respect.shared.navigation.IndicatorDetail
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.IndictorEdit
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.FabUiState

data class IndicatorListUiState(
    val indicators: List<Indicator> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class IndicatorListViewModel(
    savedStateHandle: SavedStateHandle,
    respectReportDataSource: RespectReportDataSource,
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
                    title = Res.string.indicators.asUiText(),
                    fabState = FabUiState(
                        text = Res.string.indicator.asUiText(),
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
            }
        }
    }

    fun onClickAdd() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                IndictorEdit("0")
            )
        )
    }


    fun onIndicatorSelected(indicator: Indicator) {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                IndicatorDetail(
                    indicator.indicatorId
                )
            )
        )
    }
}