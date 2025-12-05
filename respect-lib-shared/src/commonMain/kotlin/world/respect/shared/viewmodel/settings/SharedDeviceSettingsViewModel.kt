package world.respect.shared.viewmodel.settings

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.shared_device_setting
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.SelectClass
import world.respect.shared.navigation.SharedDeviceSettings
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel

data class SharedDeviceSettingsUiState(
    val requireRollNumber: Boolean = false,
)
class SharedDeviceSettingsViewModel (
    savedStateHandle: SavedStateHandle,
) : RespectViewModel(savedStateHandle) {

    private val _uiState = MutableStateFlow(SharedDeviceSettingsUiState())
    val uiState: Flow<SharedDeviceSettingsUiState> = _uiState.asStateFlow()

    init {
        _appUiState.update { prev ->
            prev.copy(
                title = Res.string.shared_device_setting.asUiText(),
                navigationVisible = true,
                hideBottomNavigation = true,
                settingsIconVisible = false
            )
        }
    }

    fun toggleRequireRollNumber(checked: Boolean) {
        _uiState.update { it.copy(requireRollNumber = checked) }
    }

    fun onClickNext(){
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(SelectClass)
        )
    }

}