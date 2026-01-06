package world.respect.shared.viewmodel.sharedschooldevicelogin

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import io.github.aakira.napier.Napier
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import world.respect.credentials.passkey.RespectQRBadgeCredential
import world.respect.datalayer.respect.model.SchoolDirectoryEntry
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.account.validateqrbadge.ValidateQrCodeUseCase
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.paste_url
import world.respect.shared.generated.resources.scan_qr_code
import world.respect.shared.navigation.ManageAccount
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.NavResultReturner
import world.respect.shared.navigation.RespectAppLauncher
import world.respect.shared.navigation.ScanQRCode
import world.respect.shared.navigation.CreateAccountSetUsername
import world.respect.shared.navigation.sendResultIfResultExpected
import world.respect.shared.resources.UiText
import world.respect.shared.util.di.SchoolDirectoryEntryScopeId
import world.respect.shared.util.exception.getUiTextOrGeneric
import world.respect.shared.util.ext.asUiText
import world.respect.shared.util.extractSchoolUrl
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.AppActionButton
import world.respect.shared.viewmodel.app.appstate.AppStateIcon
import world.respect.shared.viewmodel.app.appstate.Snack
import world.respect.shared.viewmodel.app.appstate.SnackBarDispatcher

data class ScanQRCodeUiState(
    val qrCodeUrl: String = "",
    val isLoading: Boolean = false,
    val errorMessage: UiText? = null,
    val isSuccess: Boolean = false,
    val loginErrorText: UiText? = null,
    val manualUrlError: UiText? = null,
    var showManualEntryDialog: Boolean = false,
)

class ScanQRCodeViewModel(
    savedStateHandle: SavedStateHandle,
    private val snackBarDispatcher: SnackBarDispatcher,
    private val resultReturner: NavResultReturner,
) : RespectViewModel(savedStateHandle), KoinComponent {

    private val _uiState = MutableStateFlow(ScanQRCodeUiState())
    val uiState: Flow<ScanQRCodeUiState> = _uiState.asStateFlow()

    private val route: ScanQRCode = savedStateHandle.toRoute()

    init {
        _appUiState.update { prev ->
            prev.copy(
                title = Res.string.scan_qr_code.asUiText(),
                navigationVisible = true,
                hideBottomNavigation = true,
                actions = listOf(
                    AppActionButton(
                        icon = AppStateIcon.MORE_VERT,
                        contentDescription = "more_options",
                        text = Res.string.paste_url.asUiText(),
                        onClick = {
                            _uiState.update { currentState ->
                                currentState.copy(showManualEntryDialog = true)
                            }
                        },
                        id = "more_options_qr_scan",
                        display = AppActionButton.Companion.ActionButtonDisplay.OVERFLOW_MENU
                    )
                ),
                userAccountIconVisible = false
            )
        }
    }

    fun processManualUrl(url: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    manualUrlError = null
                )
            }
            try {
                if (route.resultDest != null || route.username != null) {
                    handleQrCodeForAccountManagement(route.guid ?: "", url, isManualEntry = true)
                } else {
                    authenticateWithQrCode(Url(url), isManualEntry = true)
                }
            } catch (e: Exception) {
                Napier.e("Error processing QR Code", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.getUiTextOrGeneric()
                    )
                }
                snackBarDispatcher.showSnackBar(Snack(e.getUiTextOrGeneric()))
            }
        }
    }

    fun processQrCodeUrl(url: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    manualUrlError = null
                )
            }
            try {
                if (route.resultDest != null || route.username != null) {
                    handleQrCodeForAccountManagement(route.guid ?: "", url, isManualEntry = false)
                } else {
                    authenticateWithQrCode(Url(url), isManualEntry = false)
                }
            } catch (e: Exception) {
                Napier.e("Error processing QR Code", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.getUiTextOrGeneric()
                    )
                }
                snackBarDispatcher.showSnackBar(Snack(e.getUiTextOrGeneric()))
            }
        }
    }

    fun hideManualEntryDialog() {
        _uiState.update { currentState ->
            currentState.copy(showManualEntryDialog = false)
        }
    }

    private fun authenticateWithQrCode(url: Url, isManualEntry: Boolean) {
        launchWithLoadingIndicator {
            try {
                val credential = RespectQRBadgeCredential(qrCodeUrl = url)
                val schoolUrl = credential.extractSchoolUrl()

                if (schoolUrl == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            manualUrlError = "Invalid QR code format: Unable to extract school URL".asUiText()
                        )
                    }
                    return@launchWithLoadingIndicator
                }

                val respectAccountManager: RespectAccountManager = getKoin().get()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isSuccess = true,
                        manualUrlError = null
                    )
                }
                if (isManualEntry) {
                    _uiState.update { it.copy(showManualEntryDialog = false) }
                }

                viewModelScope.launch {
                    try {
                        respectAccountManager.login(
                            credential = credential,
                            schoolUrl = schoolUrl
                        )
                        _navCommandFlow.tryEmit(
                            NavCommand.Navigate(
                                destination = RespectAppLauncher(),
                                clearBackStack = true,
                            )
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                        _uiState.update { prev ->
                            prev.copy(
                                loginErrorText = e.getUiTextOrGeneric()
                            )
                        }
                        snackBarDispatcher.showSnackBar(Snack(e.getUiTextOrGeneric()))
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        manualUrlError = e.getUiTextOrGeneric()
                    )
                }
            }
        }
    }

    fun clearValidationError() {
        _uiState.update {
            it.copy(manualUrlError = null)
        }
    }

    private fun handleQrCodeForAccountManagement(
        personGuid: String,
        url: String,
        isManualEntry: Boolean
    ) {
        viewModelScope.launch {
            try {
                // For account management, we need the school URL from route
                val schoolUrl = route.schoolUrl ?: throw IllegalArgumentException(
                    "School URL is required for account management"
                )

                // Validate the QR code with school context
                val validationResult = validateQrCodeWithSchoolContext(url, schoolUrl)

                if (validationResult.errorMessage != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            manualUrlError = validationResult.errorMessage.asUiText()
                        )
                    }
                    snackBarDispatcher.showSnackBar(Snack(validationResult.errorMessage.asUiText()))
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            manualUrlError = null
                        )
                    }

                    if (isManualEntry) {
                        _uiState.update { it.copy(showManualEntryDialog = false) }
                    }

                    if (route.resultDest != null) {
                        // Coming from ManageAccount - use result pattern
                        resultReturner.sendResultIfResultExpected(
                            route = route,
                            navCommandFlow = _navCommandFlow,
                            result = url,
                        )
                    } else {
                        // Coming from CreateAccountSetUserName with nextAfterScan = GoToCreateAccount
                        _navCommandFlow.tryEmit(
                            NavCommand.Navigate(
                                destination = ManageAccount(
                                    guid = personGuid,
                                    qrUrlStr = url,
                                    username = route.username
                                ),
                                popUpToClass = CreateAccountSetUsername::class, // Pop up to the CreateAccount screen
                                popUpToInclusive = true // Remove CreateAccount from back stack
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.getUiTextOrGeneric(),
                        manualUrlError = e.getUiTextOrGeneric()
                    )
                }
                snackBarDispatcher.showSnackBar(Snack(e.getUiTextOrGeneric()))
                throw e
            }
        }
    }

    private fun validateQrCodeWithSchoolContext(
        qrCodeUrl: String,
        schoolUrl: Url
    ): world.respect.shared.domain.account.validateqrbadge.QrValidationResult {
        val schoolScopeId = SchoolDirectoryEntryScopeId(schoolUrl, null)
        val schoolScope = getKoin().getOrCreateScope<SchoolDirectoryEntry>(schoolScopeId.scopeId)
        val validateQrCodeUseCase: ValidateQrCodeUseCase = schoolScope.get()

        return validateQrCodeUseCase(
            qrCodeUrl = qrCodeUrl,
            personGuid = null,
        )
    }
}