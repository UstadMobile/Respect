package world.respect.shared.viewmodel.report.indictor.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import world.respect.datalayer.respect.RespectReportDataSource
import world.respect.datalayer.respect.model.Indicator
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.edit
import world.respect.shared.generated.resources.indicator_detail
import world.respect.shared.navigation.IndicatorDetail
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.IndictorEdit
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.FabUiState

data class IndicatorDetailUiState(
    val indicator: Indicator? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class IndicatorDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val respectReportDataSource: RespectReportDataSource
) : RespectViewModel(savedStateHandle) {

    private val _uiState = MutableStateFlow(IndicatorDetailUiState())
    val uiState = _uiState.asStateFlow()
    private val route: IndicatorDetail = savedStateHandle.toRoute()

    init {
        viewModelScope.launch {
            _appUiState.update { prev ->
                prev.copy(
                    navigationVisible = true,
                    title = getString(resource = Res.string.indicator_detail),
                    fabState = FabUiState(
                        text = getString(resource = Res.string.edit),
                        icon = FabUiState.FabIcon.EDIT,
                        onClick = {
                            _navCommandFlow.tryEmit(
                                NavCommand.Navigate(
                                    IndictorEdit.create(uiState.value.indicator)
                                )
                            )
                        },
                        visible = true
                    )
                )
            }

            loadIndicator()
        }
    }

    private fun loadIndicator() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val indicator = respectReportDataSource.getIndicatorById(route.indicatorUid)
                _uiState.update {
                    it.copy(
                        indicator = indicator,
                        isLoading = false,
                        errorMessage = if (indicator == null) "Indicator not found" else null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Failed to load indicator"
                    )
                }
            }
        }
    }
}