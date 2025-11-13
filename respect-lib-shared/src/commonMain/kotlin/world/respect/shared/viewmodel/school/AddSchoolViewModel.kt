package world.respect.shared.viewmodel.school

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.add_my_school
import world.respect.shared.generated.resources.done
import world.respect.shared.navigation.AddSchool
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.ActionBarButtonUiState

data class AddSchoolUiState(
    val url: String = "",
)

class AddSchoolViewModel(
    savedStateHandle: SavedStateHandle,
) : RespectViewModel(savedStateHandle) {

    private val _uiState = MutableStateFlow(AddSchoolUiState())

    val uiState = _uiState.asStateFlow()

    private val route: AddSchool = savedStateHandle.toRoute()

    init {
        _appUiState.update {
            it.copy(
                title = Res.string.add_my_school.asUiText(),
                hideBottomNavigation = true,
                actionBarButtonState = ActionBarButtonUiState(
                    visible = true,
                    text = Res.string.done.asUiText(),
                    onClick = {}
                )
            )
        }
        viewModelScope.launch {
            _uiState.update { prev ->
                prev.copy(url = route.schoolUrlStr)
            }
        }
    }
}