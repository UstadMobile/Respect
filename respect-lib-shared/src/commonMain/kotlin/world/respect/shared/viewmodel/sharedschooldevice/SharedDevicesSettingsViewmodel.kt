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
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.RespectAppDataSource
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.school.PersonDataSource
import world.respect.datalayer.school.model.Person
import world.respect.datalayer.school.model.PersonRoleEnum
import world.respect.datalayer.school.model.PersonStatusEnum
import world.respect.datalayer.shared.paging.EmptyPagingSource
import world.respect.datalayer.shared.paging.IPagingSourceFactory
import world.respect.datalayer.shared.paging.PagingSourceFactoryHolder
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.device
import world.respect.shared.generated.resources.pin_error
import world.respect.shared.generated.resources.shared_school_devices
import world.respect.shared.navigation.AcceptInvite
import world.respect.shared.navigation.InvitePerson
import world.respect.shared.navigation.NavCommand
import world.respect.shared.resources.UiText
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.FabUiState
import kotlin.random.Random

data class SharedDevicesSettingsUiState(
    val devices: IPagingSourceFactory<Int, Person> = IPagingSourceFactory {
        EmptyPagingSource()
    },
    val pendingDevices: IPagingSourceFactory<Int, Person> =
        IPagingSourceFactory { EmptyPagingSource() },
    val error: UiText? = null,
    val isPendingExpanded: Boolean = true,
    val selfSelectEnabled: Boolean = true,
    val rollNumberLoginEnabled: Boolean = true,
    val showEnableDialog: Boolean = false,
    val showPinDialog: Boolean = false,
    val pin: String = "",
    val showBottomSheetOptions: Boolean = false,
) {
    // Computed properties
    val isPinValid: Boolean
        get() = pin.length >= PIN_LENGTH && pin.all { it.isDigit() }

    companion object {
        const val PIN_LENGTH = 4
    }
}

class SharedDevicesSettingsViewmodel(
    savedStateHandle: SavedStateHandle,
    private val accountManager: RespectAccountManager,
    private val respectAppDataSource: RespectAppDataSource,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()

    private val schoolDataSource: SchoolDataSource by inject()

    private val _uiState = MutableStateFlow(SharedDevicesSettingsUiState())
    val uiState = _uiState.asStateFlow()

    private val pendingPersonsPagingSource = PagingSourceFactoryHolder {
        schoolDataSource.personDataSource.listAsPagingSource(
            DataLoadParams(),
            PersonDataSource.GetListParams(
                filterByPersonStatus = PersonStatusEnum.PENDING_APPROVAL,
                filterByPersonRole = PersonRoleEnum.SHARED_SCHOOL_DEVICE
            )
        )
    }

    private val pagingSourceFactoryHolder = PagingSourceFactoryHolder {
        schoolDataSource.personDataSource.listAsPagingSource(
            DataLoadParams(),
            PersonDataSource.GetListParams(
                filterByName = _appUiState.value.searchState.searchText.takeIf { it.isNotBlank() },
                filterByPersonStatus = PersonStatusEnum.ACTIVE,
                filterByPersonRole = PersonRoleEnum.SHARED_SCHOOL_DEVICE
            )
        )
    }

    init {
        loadSchoolPin()
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

        _uiState.update {
            it.copy(
                devices = pagingSourceFactoryHolder,
                pendingDevices = pendingPersonsPagingSource
            )
        }
    }

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
        _uiState.update { currentState ->
            currentState.copy(showBottomSheetOptions = true)
        }
    }

    fun onClickAddAnotherDevice() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                InvitePerson.create(
                    invitePersonOptions = InvitePerson.NewUserInviteOptions(
                        presetRole = PersonRoleEnum.SHARED_SCHOOL_DEVICE
                    )
                )
            )
        )
    }
    fun loadSchoolPin() {
        viewModelScope.launch {
            val pin =  Random.nextInt(1000, 10000).toString().padStart(4, '0')
            _uiState.update { it.copy(pin = pin) }
        }
        onSavePin()
    }

    fun onClickEnableOnThisDevice() {
//        _navCommandFlow.tryEmit(
//            NavCommand.Navigate(
//                AcceptInvite.create(null)
//            )
//        )
    }

    fun onShowPinDialog() {
        _uiState.update { it.copy(showPinDialog = true) }
    }

    fun onDismissPinDialog() {
        _uiState.update { it.copy(showPinDialog = false) }
    }

    fun onPinChange(newPin: String) {
        _uiState.update { it.copy(pin = newPin) }

    }

    fun onSavePin() {
        val currentPin = _uiState.value.pin
        if (currentPin.length >= SharedDevicesSettingsUiState.PIN_LENGTH && currentPin.all { it.isDigit() }) {
            viewModelScope.launch {
                // TODO: Implement actual pin saving
                onDismissPinDialog()
            }
        } else {
            _uiState.update { it.copy(error = Res.string.pin_error.asUiText()) }
        }
    }

    fun onTogglePendingInvites() {
        _uiState.update {
            it.copy(isPendingExpanded = !it.isPendingExpanded)
        }
    }

    fun onDismissBottomSheet() {
        _uiState.update { currentState ->
            currentState.copy(showBottomSheetOptions = false)
        }
    }

    fun onApproveDevice(deviceGuid: String) {
        viewModelScope.launch {
            // TODO: Implement approve logic
        }
    }

    fun onRejectDevice(deviceGuid: String) {
        viewModelScope.launch {
            // TODO: Implement reject logic
        }
    }

    fun onRemoveDevice(deviceGuid: String) {


    }
}