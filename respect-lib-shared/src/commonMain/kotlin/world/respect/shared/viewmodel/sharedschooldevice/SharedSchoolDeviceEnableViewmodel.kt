package world.respect.shared.viewmodel.sharedschooldevice

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.shared_school_devices
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.RespectAppLauncher
import world.respect.shared.resources.UiText
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel

data class SharedSchoolDeviceEnableUiState(
    val error: UiText? = null,
    val deviceName: String = ""
)

class SharedSchoolDeviceEnableViewmodel(
    savedStateHandle: SavedStateHandle,
) : RespectViewModel(savedStateHandle) {

    private val _uiState = MutableStateFlow(SharedSchoolDeviceEnableUiState())
    val uiState = _uiState.asStateFlow()

    init {
        _appUiState.update {
            it.copy(
                title = Res.string.shared_school_devices.asUiText(),
                hideBottomNavigation = true,
                showBackButton = false,
            )
        }
    }

    fun updateDeviceName(deviceName: String) {
        _uiState.update { currentState ->
            currentState.copy(deviceName = deviceName)
        }
    }

    fun enableSharedDeviceMode() {
        // TODO: Implement saving to database
        val deviceName = _uiState.value.deviceName
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(RespectAppLauncher())
        )
    }
}