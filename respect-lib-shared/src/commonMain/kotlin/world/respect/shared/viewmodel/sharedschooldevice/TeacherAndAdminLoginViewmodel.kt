package world.respect.shared.viewmodel.sharedschooldevice

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.teacher_admin_login
import world.respect.shared.navigation.GetStartedScreen
import world.respect.shared.navigation.NavCommand
import world.respect.shared.resources.UiText
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel

data class TeacherAndAdminLoginUiState(
    val errorMessage: UiText? = null,
    val pin: String = "",
)

class TeacherAndAdminLoginViewmodel(
    savedStateHandle: SavedStateHandle,
) : RespectViewModel(savedStateHandle) {

    private val _uiState = MutableStateFlow(TeacherAndAdminLoginUiState())

    val uiState = _uiState.asStateFlow()


    init {
        _appUiState.update {
            it.copy(
                title = Res.string.teacher_admin_login.asUiText(),
                hideBottomNavigation = true,
            )
        }
    }

    fun onPinChanged(pin: String) {
        _uiState.update { it.copy(pin = pin) }
    }

    fun onClickNext() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(GetStartedScreen())
        )
    }
}