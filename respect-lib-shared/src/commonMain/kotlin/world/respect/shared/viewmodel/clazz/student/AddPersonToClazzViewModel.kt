package world.respect.shared.viewmodel.clazz.student

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import world.respect.shared.generated.resources.Res
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.generated.resources.add_student


data class AddPersonToClazzUIState(
    val name:String=""
)
class AddPersonToClazzViewModel(
    savedStateHandle: SavedStateHandle,
) : RespectViewModel(savedStateHandle) {

    private val _uiState = MutableStateFlow(AddPersonToClazzUIState())

    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _appUiState.update {
                it.copy(
                    title= getString(Res.string.add_student)
                )
            }
        }
    }
}