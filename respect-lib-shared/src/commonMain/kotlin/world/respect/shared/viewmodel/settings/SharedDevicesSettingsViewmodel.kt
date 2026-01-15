package world.respect.shared.viewmodel.settings

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.Json
import world.respect.datalayer.school.model.Person
import world.respect.datalayer.school.model.PersonRoleEnum
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.device
import world.respect.shared.generated.resources.shared_school_devices
import world.respect.shared.navigation.InvitePerson
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.NavResultReturner
import world.respect.shared.resources.UiText
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.FabUiState

data class SharedDevicesSettingsUiState(
    val school: List<Person> = emptyList(),
    val error: UiText? = null,
    val selfSelectEnabled: Boolean = true,
    val rollNumberLoginEnabled: Boolean = true,
    val showEnableDialog: Boolean = false,
    val deviceName: String = ""
)

class SharedDevicesSettingsViewmodel(
    savedStateHandle: SavedStateHandle,
    private val json: Json,
    private val resultReturner: NavResultReturner,
) : RespectViewModel(savedStateHandle) {

    private val _uiState = MutableStateFlow(SharedDevicesSettingsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        _appUiState.update {
            it.copy(
                title = Res.string.shared_school_devices.asUiText(),
                hideBottomNavigation = true,
                fabState = FabUiState(
                    text = Res.string.device.asUiText(),
                    icon = FabUiState.FabIcon.ADD,
                    onClick = ::onClickAdd,
                    visible = true,
                ),
                showBackButton = false,
            )
        }
    }

    // Functions to handle toggles
    fun toggleSelfSelect(enabled: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(selfSelectEnabled = enabled)
        }
    }

    fun toggleRollNumberLogin(enabled: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(rollNumberLoginEnabled = enabled)
        }
    }

    fun onClickAdd() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                InvitePerson.create(
                    inviteCode = null,
                    presetRole = PersonRoleEnum.SHARED_SCHOOL_DEVICE
                )
            )
        )
    }

    fun onClickEnableSharedSchoolDeviceMode() {
        _uiState.update { currentState ->
            currentState.copy(showEnableDialog = true)
        }
    }

    fun onDismissEnableDialog() {
        _uiState.update { currentState ->
            currentState.copy(showEnableDialog = false)
        }
    }

    fun onConfirmEnableDialog(localDeviceName: String) {
        onDismissEnableDialog()
    }
}