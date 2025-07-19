package world.respect.shared.viewmodel.clazz

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import world.respect.shared.viewmodel.RespectViewModel
import org.jetbrains.compose.resources.getString
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.classes

class ClazzViewModel(
    savedStateHandle: SavedStateHandle
): RespectViewModel(savedStateHandle) {
    init {
        viewModelScope.launch {
            _appUiState.update {
                it.copy(
                    title = getString(resource = Res.string.classes),
                    showBackButton = false,
                )
            }
        }
    }

}