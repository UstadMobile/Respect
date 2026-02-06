package world.respect.shared.viewmodel.sharedschooldevice.login

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import world.respect.datalayer.school.model.Clazz
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.select_class
import world.respect.shared.resources.UiText
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel

data class SelectClassUiState(
    val error: UiText? = null,
    val clazz: List<Clazz> = listOf(Clazz(guid = "11", title = "claasss")),
)

class SelectClassViewmodel(
    savedStateHandle: SavedStateHandle,
) : RespectViewModel(savedStateHandle) {

    private val _uiState = MutableStateFlow(SelectClassUiState())

    val uiState = _uiState.asStateFlow()


    init {
        _appUiState.update {
            it.copy(
                title = Res.string.select_class.asUiText(),
                hideBottomNavigation = true,
            )
        }
    }

    fun onClickScanQrCode(){
//        _navCommandFlow.tryEmit(
//            NavCommand.Navigate(SharedDevicesSettings)
//        )
    }
    fun onClickTeacherAdminLogin(){
//        _navCommandFlow.tryEmit(
//            NavCommand.Navigate(SharedDevicesSettings)
//        )
    }
}