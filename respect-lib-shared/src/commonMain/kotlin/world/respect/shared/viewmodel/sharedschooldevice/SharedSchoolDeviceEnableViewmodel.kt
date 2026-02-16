package world.respect.shared.viewmodel.sharedschooldevice

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.school.model.Invite2
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.account.RespectSessionAndPerson
import world.respect.shared.domain.account.invite.EnableSharedDeviceModeUseCase
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.shared_school_devices
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.SelectClass
import world.respect.shared.resources.UiText
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel

data class SharedSchoolDeviceEnableUiState(
    val error: UiText? = null,
    val deviceName: String = "",
    val selectedAccount: RespectSessionAndPerson? = null,
    val isEnabling: Boolean = false,
    val isSuccess: Boolean = false
) {
    val isDeviceNameValid: Boolean
        get() = deviceName.isNotBlank()
}

class SharedSchoolDeviceEnableViewmodel(
    savedStateHandle: SavedStateHandle,
    private val respectAccountManager: RespectAccountManager,
    private val enableSharedDeviceModeUseCase: EnableSharedDeviceModeUseCase
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = respectAccountManager.requireActiveAccountScope()
    private val schoolDataSource: SchoolDataSource by inject() // From account scope

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
        viewModelScope.launch {
            respectAccountManager.selectedAccountAndPersonFlow.collect { accountAndPerson ->
                _uiState.update { prev ->
                    prev.copy(selectedAccount = accountAndPerson)
                }
            }
        }
    }

    fun updateDeviceName(deviceName: String) {
        _uiState.update { currentState ->
            currentState.copy(deviceName = deviceName)
        }
    }

    fun enableSharedDeviceMode() {
        val deviceName = _uiState.value.deviceName

        if (deviceName.isBlank()) {
            _uiState.update { it.copy(error = "Please enter a device name".asUiText()) }
            return
        }

        _uiState.update { it.copy(isEnabling = true, error = null) }

        viewModelScope.launch {
            try {
                val schoolUrl = respectAccountManager.activeAccount?.school?.self
                    ?: throw IllegalStateException("No active school session found")

                val inviteCode = Invite2.newRandomCode()

                enableSharedDeviceModeUseCase(
                    inviteCode = inviteCode,
                    deviceName = deviceName,
                    schoolUrl = schoolUrl
                )
                _navCommandFlow.tryEmit(NavCommand.Navigate(SelectClass))

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isEnabling = false,
                        error = "Failed to enable shared device mode: ${e.message}".asUiText()
                    )
                }
            }
        }
    }

    private fun saveSharedDeviceSettings(deviceName: String) {
        // TODO: Implement saving shared device mode to database
        println("Shared device mode enabled with name: $deviceName")
    }
}