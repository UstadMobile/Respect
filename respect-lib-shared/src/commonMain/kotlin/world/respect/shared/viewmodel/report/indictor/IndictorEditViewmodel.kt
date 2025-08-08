package world.respect.shared.viewmodel.report.indictor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import world.respect.shared.domain.report.model.Indicator
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.done
import world.respect.shared.generated.resources.indicator
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.ReportEdit
import world.respect.shared.navigation.ReportIndictorEdit
import world.respect.shared.resources.UiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.ActionBarButtonUiState

data class IndicatorEditUiState(
    val indicatorData: Indicator = Indicator(),
    val nameError: UiText? = null,
    val descriptionError: UiText? = null,
    val sqlError: UiText? = null
)

class IndicatorEditViewModel(
    savedStateHandle: SavedStateHandle
) : RespectViewModel(savedStateHandle) {

    private val _uiState = MutableStateFlow(IndicatorEditUiState())
    val uiState = _uiState.asStateFlow()

    private val route: ReportIndictorEdit = savedStateHandle.toRoute()

    init {
        viewModelScope.launch {
            // Initialize with existing indicator data if available
            route.indicatorData?.let { indicator ->
                _uiState.update { it.copy(indicatorData = indicator) }
            }

            _appUiState.update { prev ->
                prev.copy(
                    navigationVisible = true,
                    title = getString(resource = Res.string.indicator),
                    actionBarButtonState = ActionBarButtonUiState(
                        visible = true,
                        text = getString(resource = Res.string.done),
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
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                ReportEdit.create(
                    reportUid = route.reportUid,
                    indicator = _uiState.value.indicatorData
                )
            )
        )
    }
}