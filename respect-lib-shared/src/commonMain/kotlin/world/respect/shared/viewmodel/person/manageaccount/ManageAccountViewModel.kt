package world.respect.shared.viewmodel.person.manageaccount

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.credentials.passkey.CheckPasskeySupportUseCase
import world.respect.credentials.passkey.CreatePasskeyUseCase
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.DataLoadingState
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.db.school.ext.isStudent
import world.respect.datalayer.ext.dataOrNull
import world.respect.datalayer.school.adapters.toPersonPasskey
import world.respect.datalayer.school.findByPersonGuidAsFlow
import world.respect.datalayer.school.model.PersonBadge
import world.respect.datalayer.school.model.PersonPassword
import world.respect.datalayer.school.model.StatusEnum
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.account.RespectSessionAndPerson
import world.respect.shared.domain.getdeviceinfo.GetDeviceInfoUseCase
import world.respect.shared.domain.getdeviceinfo.toUserFriendlyString
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.manage_account
import world.respect.shared.navigation.ChangePassword
import world.respect.shared.navigation.HowPasskeyWorks
import world.respect.shared.navigation.ManageAccount
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.NavResultReturner
import world.respect.shared.navigation.PasskeyList
import world.respect.shared.navigation.RouteResultDest
import world.respect.shared.navigation.ScanQRCode
import world.respect.shared.resources.StringUiText
import world.respect.shared.resources.UiText
import world.respect.shared.util.UrlParser
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.Snack
import world.respect.shared.viewmodel.app.appstate.SnackBarDispatcher
import kotlin.time.Clock

data class ManageAccountUiState(
    val accountGuid: String = "",
    val passkeyCount: Int? = null,
    val passkeySupported: Boolean = false,
    val personUsername: String = "",
    val personPassword: DataLoadState<PersonPassword> = DataLoadingState(),
    val errorText: UiText? = null,
    val selectedAccount: RespectSessionAndPerson? = null,
    val isStudent: Boolean = false,
    val qrBadge: DataLoadState<PersonBadge> = DataLoadingState(),
    val showBottomSheet: Boolean = false,
    val qrCodeBadgeError: UiText? = null,
) {

    val showCreatePasskey: Boolean
        get() = passkeyCount == 0 && passkeySupported &&
                selectedAccount?.person?.guid == accountGuid

    val showManagePasskey: Boolean
        get() = passkeyCount != null && passkeyCount > 0

    val badgeNumber: String?
        get() = qrBadge.dataOrNull()?.qrCodeUrl?.let { UrlParser.extractBadgeNumberFromUrl(it) }

    val isQrAdded: Boolean
        get() = badgeNumber != null
}

class ManageAccountViewModel(
    savedStateHandle: SavedStateHandle,
    private val accountManager: RespectAccountManager,
    private val getDeviceInfoUseCase: GetDeviceInfoUseCase,
    private val json: Json,
    private val snackBarDispatcher: SnackBarDispatcher,
    private val navResultReturner: NavResultReturner
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()

    private val checkPasskeySupportUseCase: CheckPasskeySupportUseCase by lazy {
        scope.get()
    }

    private val createPasskeyUseCase: CreatePasskeyUseCase? by lazy {
        scope.getOrNull()
    }

    private val schoolDataSource: SchoolDataSource by inject()

    private val route: ManageAccount = savedStateHandle.toRoute()

    private val personGuid = route.guid

    private val _uiState = MutableStateFlow(
        ManageAccountUiState(
            accountGuid = personGuid
        )
    )

    val uiState = _uiState.asStateFlow()

    init {
        _appUiState.update { prev ->
            prev.copy(
                userAccountIconVisible = false,
                navigationVisible = false,
                title = Res.string.manage_account.asUiText()
            )
        }

        viewModelScope.launch {
            if (route.qrUrl != null && route.username != null) {
                saveUsername(route.username)
                storeQrCodeForPerson(personGuid = personGuid, url = route.qrUrl.toString())
            }
        }

        viewModelScope.launch {
            navResultReturner.filteredResultFlowForKey(
                QR_SELECT_RESULT
            ).collect { navResult ->
                val qrUrl = navResult.result as? String ?: return@collect
                storeQrCodeForPerson(personGuid = personGuid, url = qrUrl)
            }
        }

        viewModelScope.launch {
            schoolDataSource.personPasswordDataSource.findByPersonGuidAsFlow(
                route.guid
            ).collect { password ->
                _uiState.update { it.copy(personPassword = password) }
            }
        }

        viewModelScope.launch {
            schoolDataSource.personQrDataSource.findByPersonGuidAsFlow(
                route.guid
            ).collect { qrBadgeState ->
                _uiState.update {
                    it.copy(
                        qrBadge = qrBadgeState,
                        qrCodeBadgeError = null // Clear error when data loads successfully
                    )
                }
            }
        }

        viewModelScope.launch {
            if (checkPasskeySupportUseCase()) {
                _uiState.update { prev ->
                    prev.copy(passkeySupported = true)
                }
            }
        }

        viewModelScope.launch {
            launch {
                schoolDataSource.personPasskeyDataSource.listAllAsFlow().collect {
                    _uiState.update { prev ->
                        prev.copy(
                            passkeyCount = it.dataOrNull()?.size ?: 0,
                        )
                    }
                }
            }

            launch {
                schoolDataSource.personDataSource.findByGuidAsFlow(
                    route.guid
                ).collect {
                    _uiState.update { prev ->
                        prev.copy(
                            personUsername = it.dataOrNull()?.username ?: "",
                            isStudent = it.dataOrNull()?.isStudent() == true
                        )
                    }
                }
            }
        }

        viewModelScope.launch {
            accountManager.selectedAccountAndPersonFlow.collect { accountAndPerson ->
                _uiState.update { prev ->
                    prev.copy(selectedAccount = accountAndPerson)
                }
            }
        }

        _uiState.update { prev ->
            prev.copy(
                passkeySupported = (createPasskeyUseCase != null &&
                        accountManager.activeAccount?.userGuid == personGuid),
            )
        }
    }

    fun onClickChangePassword() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                ChangePassword(guid = route.guid)
            )
        )
    }

    fun onClickManagePasskey() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                PasskeyList(
                    guid = route.guid
                )
            )
        )
    }

    fun onClickHowPasskeysWork() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(HowPasskeyWorks)
        )
    }

    fun onClickQRCodeBadge() {
        val schoolUrl = accountManager.activeAccount?.school?.self
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                ScanQRCode.create(
                    guid = personGuid,
                    schoolUrl = schoolUrl,
                    resultDest = RouteResultDest(
                        resultPopUpTo = route,
                        resultKey = QR_SELECT_RESULT
                    )
                )
            )
        )
    }

    fun onDismissBottomSheet() {
        _uiState.update { prev ->
            prev.copy(
                showBottomSheet = false,
            )
        }
    }

    fun onRemoveQRBadge() {
        viewModelScope.launch {
            try {
                val currentQrBadge = uiState.value.qrBadge.dataOrNull()
                if (currentQrBadge != null) {
                    schoolDataSource.personQrDataSource.store(
                        listOf(
                            currentQrBadge.copy(
                                status = StatusEnum.TO_BE_DELETED,
                                lastModified = Clock.System.now(),
                            )
                        )
                    )
                }
            } catch (e: Exception) {
                _uiState.update { prev ->
                    prev.copy(
                        qrCodeBadgeError = StringUiText("Failed to remove QR badge: ${e.message}")
                    )
                }
            }
        }
    }

    private fun storeQrCodeForPerson(personGuid: String, url: String) {
        viewModelScope.launch {
            try {
                val qrCodeAlreadyAssignedToAnotherPerson =
                    schoolDataSource.personQrDataSource.existsByQrCodeUrl(url, personGuid.toLong())

                if (qrCodeAlreadyAssignedToAnotherPerson) {
                    _uiState.update { prev ->
                        prev.copy(
                            qrCodeBadgeError = StringUiText("This QR code is already assigned to another student")
                        )
                    }
                    snackBarDispatcher.showSnackBar(
                        Snack("This QR code is already assigned to another student".asUiText())
                    )
                } else {
                    val now = Clock.System.now()
                    schoolDataSource.personQrDataSource.store(
                        listOf(
                            PersonBadge(
                                personGuid = personGuid,
                                qrCodeUrl = url,
                                lastModified = now,
                                stored = now,
                                status = StatusEnum.ACTIVE,
                            )
                        )
                    )
                    _uiState.update { prev ->
                        prev.copy(qrCodeBadgeError = null) // Clear error on success
                    }
                }
            } catch (e: Exception) {
                _uiState.update { prev ->
                    prev.copy(
                        qrCodeBadgeError = StringUiText("Failed to assign QR code: ${e.message}")
                    )
                }
                throw e
            }
        }
    }

    private fun saveUsername(username: String?) {
        viewModelScope.launch {
            val person = schoolDataSource.personDataSource.findByGuid(
                DataLoadParams(), route.guid
            ).dataOrNull() ?: throw IllegalStateException("Person not found")

            schoolDataSource.personDataSource.store(
                listOf(
                    person.copy(
                        username = username,
                        lastModified = Clock.System.now(),
                    )
                )
            )
        }
    }

    fun onClickChangeQrBadge() {
        _uiState.update { prev ->
            prev.copy(
                showBottomSheet = true,
            )
        }
    }

    fun onCreatePasskeyClick() {
        viewModelScope.launch {
            val passkeyCreated = createPasskeyUseCase?.invoke(
                CreatePasskeyUseCase.Request(
                    personUid = uiState.value.selectedAccount?.person?.guid ?: return@launch,
                    username = uiState.value.selectedAccount?.person?.username ?: return@launch,
                    rpId = uiState.value.selectedAccount?.session?.account?.school?.rpId
                        ?: return@launch
                )
            )

            if (passkeyCreated != null) {
                when (passkeyCreated) {
                    is CreatePasskeyUseCase.PasskeyCreatedResult -> {
                        schoolDataSource.personPasskeyDataSource.store(
                            listOf(
                                passkeyCreated.toPersonPasskey(
                                    json = json,
                                    personGuid = personGuid,
                                    deviceName = getDeviceInfoUseCase().toUserFriendlyString(),
                                )
                            )
                        )
                    }

                    is CreatePasskeyUseCase.Error -> {
                        _uiState.update { prev ->
                            prev.copy(
                                errorText = StringUiText(passkeyCreated.message ?: ""),
                            )
                        }
                    }

                    is CreatePasskeyUseCase.UserCanceledResult -> {
                        //do nothing
                    }
                }
            }
        }
    }

    companion object {
        const val QR_SELECT_RESULT = "qr_select_result"
    }
}