package world.respect.shared.viewmodel.report

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.report
import world.respect.shared.viewmodel.RespectViewModel


class ReportViewModel(
    savedStateHandle: SavedStateHandle
) : RespectViewModel(savedStateHandle) {

    init {
        viewModelScope.launch {
            _appUiState.update {
                it.copy(
                    title = getString(resource = Res.string.report),
                    showBackButton = false,
                )
            }
        }
    }

}
