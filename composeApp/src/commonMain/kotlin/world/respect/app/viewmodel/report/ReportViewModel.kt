package world.respect.app.viewmodel.report

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import respect.composeapp.generated.resources.Res
import respect.composeapp.generated.resources.report
import world.respect.app.viewmodel.RespectViewModel


class ReportViewModel(
) : RespectViewModel() {

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
