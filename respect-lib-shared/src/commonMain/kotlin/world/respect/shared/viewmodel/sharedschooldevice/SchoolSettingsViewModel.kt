package world.respect.shared.viewmodel.sharedschooldevice

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.Json
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.school
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.NavResultReturner
import world.respect.shared.navigation.SharedDevicesSettings
import world.respect.shared.resources.UiText
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel

data class SchoolSettingsUiState(
    val school: String? = null,
    val error: UiText? = null,
    val sharedSchoolDeviceCount: String? = null
)

class SchoolSettingsViewModel(
    savedStateHandle: SavedStateHandle,
    private val json: Json,
    private val resultReturner: NavResultReturner,
) : RespectViewModel(savedStateHandle) {

    private val _uiState = MutableStateFlow(SchoolSettingsUiState())

    val uiState = _uiState.asStateFlow()

    init {
        _appUiState.update {
            it.copy(
                title = Res.string.school.asUiText(),
                hideBottomNavigation = true,
            )
        }
    }

    fun onClickSharedSchoolDevices() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(SharedDevicesSettings)
        )
    }
}