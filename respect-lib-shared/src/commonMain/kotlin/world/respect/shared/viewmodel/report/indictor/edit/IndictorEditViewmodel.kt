package world.respect.shared.viewmodel.report.indictor.edit

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
import world.respect.shared.generated.resources.done
import world.respect.shared.generated.resources.indicator
import world.respect.shared.navigation.IndicatorList
import world.respect.shared.navigation.IndictorEdit
import world.respect.shared.navigation.NavCommand
import world.respect.shared.resources.UiText
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.ActionBarButtonUiState

data class IndicatorEditUiState(
    val indicatorData: Indicator = Indicator(),
    val nameError: UiText? = null,
    val descriptionError: UiText? = null,
    val sqlError: UiText? = null
)

class IndicatorEditViewModel(
    savedStateHandle: SavedStateHandle,
    private val respectReportDataSource: RespectReportDataSource,
) : RespectViewModel(savedStateHandle) {

    private val _uiState = MutableStateFlow(IndicatorEditUiState())
    val uiState = _uiState.asStateFlow()

    private val route: IndictorEdit = savedStateHandle.toRoute()

    init {
        viewModelScope.launch {
            route.indicatorId.let { indicatorId ->
                val indicators = respectReportDataSource.getIndicatorById(indicatorId)
                indicators?.let {
                    _uiState.update { it.copy(indicatorData = indicators) }
                }
            }

            _appUiState.update { prev ->
                prev.copy(
                    navigationVisible = true,
                    title = Res.string.indicator.asUiText(),
                    actionBarButtonState = ActionBarButtonUiState(
                        visible = true,
                        text = Res.string.done.asUiText(),
                        onClick = this@IndicatorEditViewModel::onSaveIndicator
                    ),
                    userAccountIconVisible = false,
                )
            }
        }
    }

    fun updateIndicator(updateFn: (Indicator) -> Indicator) {
        _uiState.update { state ->
            state.copy(indicatorData = updateFn(state.indicatorData))
        }
    }


    private fun onSaveIndicator() {
        viewModelScope.launch {
            respectReportDataSource.saveIndicator(_uiState.value.indicatorData)
        }
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                IndicatorList
            )
        )
    }
}