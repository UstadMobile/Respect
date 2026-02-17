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
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.db.school.ext.isAdminOrTeacher
import world.respect.datalayer.ext.dataOrNull
import world.respect.datalayer.school.PersonDataSource
import world.respect.datalayer.school.ext.newUserInviteUid
import world.respect.datalayer.school.model.Invite2
import world.respect.datalayer.school.model.NewUserInvite
import world.respect.datalayer.school.model.Person
import world.respect.datalayer.school.model.PersonRoleEnum
import world.respect.datalayer.school.model.PersonStatusEnum
import world.respect.datalayer.shared.paging.EmptyPagingSource
import world.respect.datalayer.shared.paging.IPagingSourceFactory
import world.respect.datalayer.shared.paging.PagingSourceFactoryHolder
import world.respect.datalayer.shared.params.GetListCommonParams
import world.respect.libutil.ext.CHAR_POOL_NUMBERS
import world.respect.libutil.ext.randomString
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.account.invite.ApproveOrDeclineInviteRequestUseCase
import world.respect.shared.ext.tryOrShowSnackbarOnError
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
import world.respect.shared.viewmodel.app.appstate.SnackBarDispatcher
import kotlin.random.Random
import kotlin.time.Clock

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
    val isPinValid: Boolean
        get() = pin.length >= PIN_LENGTH && pin.all { it.isDigit() }

    companion object {
        const val PIN_LENGTH = 4
    }
}

class SharedDevicesSettingsViewmodel(
    savedStateHandle: SavedStateHandle,
    private val accountManager: RespectAccountManager,
    private val snackBarDispatcher: SnackBarDispatcher,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()
    private val schoolDataSource: SchoolDataSource by inject()
    private val approveOrDeclineInviteRequestUseCase: ApproveOrDeclineInviteRequestUseCase by inject()

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
            val pin = Random.nextInt(1000, 10000).toString().padStart(4, '0')
            _uiState.update { it.copy(pin = pin) }
        }
        onSavePin()
    }

    fun onClickEnableOnThisDevice() {
        viewModelScope.launch {
            val activeAccount = accountManager.activeAccount
            val persons = schoolDataSource.personDataSource.list(
                loadParams = DataLoadParams(),
                params = PersonDataSource.GetListParams(
                    common = GetListCommonParams(
                        guid = activeAccount?.userGuid
                    ),
                    includeRelated = true,
                )
            ).dataOrNull()
            val activePerson = persons?.firstOrNull { person ->
                person.guid == (activeAccount?.userGuid)
            }
            val isTeacherOrAdmin = activePerson?.isAdminOrTeacher() ?: false

            activeAccount?.school?.self?.let { url ->
                val invite = createSharedDeviceInvite()
                _navCommandFlow.tryEmit(
                    NavCommand.Navigate(
                        AcceptInvite.create(
                            schoolUrl = url,
                            code = invite.code,
                            isTeacherOrAdmin = isTeacherOrAdmin
                        )
                    )
                )
            }
        }
    }

    private suspend fun createSharedDeviceInvite(): Invite2 {
        // Create a new invite for SHARED_SCHOOL_DEVICE role
        val inviteUid = PersonRoleEnum.SHARED_SCHOOL_DEVICE.newUserInviteUid

        // Check if invite already exists
        val existingInvite = schoolDataSource.inviteDataSource.findByGuid(
            guid = inviteUid,
        ).dataOrNull()

        if (existingInvite != null) {
            return existingInvite
        }

        // Create new invite
        val newInvite = NewUserInvite(
            uid = inviteUid,
            code = randomString(10, CHAR_POOL_NUMBERS),
            role = PersonRoleEnum.SHARED_SCHOOL_DEVICE,
            approvalRequiredAfter = Clock.System.now(),
        )
        schoolDataSource.inviteDataSource.store(listOf(newInvite))
        return newInvite
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

    fun onClickAcceptOrDismissInvite(
        person: Person,
        approved: Boolean,
    ) {
        viewModelScope.launch {
            snackBarDispatcher.tryOrShowSnackbarOnError {
                approveOrDeclineInviteRequestUseCase(
                    personUid = person.guid,
                    approved = approved,
                )
            }
        }
    }

    fun onRemoveDevice(person: Person) {
        viewModelScope.launch {
            schoolDataSource.personDataSource.store(
                listOf(
                    person.copy(
                        status = PersonStatusEnum.TO_BE_DELETED,
                        lastModified = Clock.System.now(),
                    )
                )
            )
        }
    }
}