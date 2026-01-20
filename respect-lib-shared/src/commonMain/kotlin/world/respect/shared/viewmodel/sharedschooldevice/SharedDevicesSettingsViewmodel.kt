package world.respect.shared.viewmodel.sharedschooldevice

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.school.PersonDataSource
import world.respect.datalayer.school.model.Person
import world.respect.datalayer.school.model.PersonRoleEnum
import world.respect.datalayer.shared.paging.EmptyPagingSource
import world.respect.datalayer.shared.paging.IPagingSourceFactory
import world.respect.datalayer.shared.paging.PagingSourceFactoryHolder
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.device
import world.respect.shared.generated.resources.shared_school_devices
import world.respect.shared.navigation.InvitePerson
import world.respect.shared.navigation.NavCommand
import world.respect.shared.resources.UiText
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.FabUiState

data class SharedDevicesSettingsUiState(
    val devices: IPagingSourceFactory<Int, Person> = IPagingSourceFactory {
        EmptyPagingSource()
    },
    val error: UiText? = null,
    val selfSelectEnabled: Boolean = true,
    val rollNumberLoginEnabled: Boolean = true,
    val showEnableDialog: Boolean = false,
    val deviceName: String = ""
)

class SharedDevicesSettingsViewmodel(
    savedStateHandle: SavedStateHandle,
    accountManager: RespectAccountManager,
    ) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()

    private val schoolDataSource: SchoolDataSource by inject()

    private val _uiState = MutableStateFlow(SharedDevicesSettingsUiState())
    val uiState = _uiState.asStateFlow()

    private val pagingSourceFactoryHolder = PagingSourceFactoryHolder {
        schoolDataSource.personDataSource.listAsPagingSource(
            DataLoadParams(),
            PersonDataSource.GetListParams(
                filterByName = _appUiState.value.searchState.searchText.takeIf { it.isNotBlank() },
            )
        )
    }

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

        _uiState.update {
            it.copy(
                devices = pagingSourceFactoryHolder,
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