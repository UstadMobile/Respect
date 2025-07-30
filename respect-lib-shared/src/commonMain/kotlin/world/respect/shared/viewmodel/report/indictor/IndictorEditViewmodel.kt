package world.respect.shared.viewmodel.report.indictor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import world.respect.datalayer.respect.RespectReportDataSource
import world.respect.shared.domain.report.model.IndicatorData
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.done
import world.respect.shared.generated.resources.indicator
import world.respect.shared.resources.UiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.ActionBarButtonUiState

data class IndicatorEditUiState(
    val indicatorData: IndicatorData = IndicatorData(),
    val nameError: UiText? = null,
    val descriptionError: UiText? = null,
    val sqlError: UiText? = null
)

class IndictorEditViewmodel(
    savedStateHandle: SavedStateHandle,
    private val respectReportDataSource: RespectReportDataSource
) : RespectViewModel(savedStateHandle) {

    private val _uiState = MutableStateFlow(IndicatorEditUiState())
    val uiState: Flow<IndicatorEditUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _appUiState.update { prev ->
                prev.copy(
                    navigationVisible = true,
                    title = getString(resource = Res.string.indicator),
                    actionBarButtonState = ActionBarButtonUiState(
                        visible = true,
                        text = getString(resource = Res.string.done),
                        onClick = this@IndictorEditViewmodel::onClickSave
                    ),
                    userAccountIconVisible = false,
                )
            }
        }
    }

    fun onClickSave() {
        viewModelScope.launch {

        }
    }
}