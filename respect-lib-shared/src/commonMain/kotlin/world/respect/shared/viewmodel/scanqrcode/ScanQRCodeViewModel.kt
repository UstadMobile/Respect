package world.respect.shared.viewmodel.scanqrcode

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.component.KoinComponent
import world.respect.credentials.passkey.RespectQRBadgeCredential
import world.respect.datalayer.respect.model.SchoolDirectoryEntry
import world.respect.libutil.ext.schoolUrlOrNull
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.account.validateqrbadge.ValidateQrCodeUseCase
import world.respect.shared.ext.NextAfterScan
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.more_options
import world.respect.shared.generated.resources.paste_url
import world.respect.shared.generated.resources.qr_code_invalid_format
import world.respect.shared.generated.resources.scan_qr_code
import world.respect.shared.navigation.CreateAccountSetUsername
import world.respect.shared.navigation.ManageAccount
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.NavResultReturner
import world.respect.shared.navigation.RespectAppLauncher
import world.respect.shared.navigation.ScanQRCode
import world.respect.shared.navigation.sendResultIfResultExpected
import world.respect.shared.resources.UiText
import world.respect.shared.util.di.SchoolDirectoryEntryScopeId
import world.respect.shared.util.exception.getUiTextOrGeneric
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.AppActionButton
import world.respect.shared.viewmodel.app.appstate.AppStateIcon

data class ScanQRCodeUiState(
    val errorMessage: UiText? = null,
    val showManualEntryDialog: Boolean = false,
)

class ScanQRCodeViewModel(
    savedStateHandle: SavedStateHandle,
    private val resultReturner: NavResultReturner,
    private val respectAccountManager: RespectAccountManager,
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
                        contentDescription = Res.string.more_options.asUiText(),
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

    fun onQrCodeScanned(url: String) {
        launchWithLoadingIndicator {
            try {
                if (route.nextAfterScan == NextAfterScan.GoToManageAccount) {
                    handleQrCodeForAccountManagement(route.guid ?: "", url)
                } else {
                    authenticateWithQrCode(Url(url))
                }
            } catch (e: Throwable) {
                _uiState.update {
                    it.copy(
                        errorMessage = e.getUiTextOrGeneric()
                    )
                }
            }
        }
    }

    fun hideManualEntryDialog() {
        _uiState.update { currentState ->
            currentState.copy(showManualEntryDialog = false)
        }
    }

    fun onQrCodeScanError(exception: Exception) {
        _uiState.update {
            it.copy(
                errorMessage = exception.getUiTextOrGeneric(),
            )
        }
    }

    private suspend fun authenticateWithQrCode(url: Url) {
        val credential = RespectQRBadgeCredential(qrCodeUrl = url)
        val schoolUrl = url.schoolUrlOrNull()

        if (schoolUrl == null) {
            _uiState.update {
                it.copy(
                    errorMessage = Res.string.qr_code_invalid_format.asUiText(),
                    showManualEntryDialog = false
                )
            }

            _appUiState.update { prev ->
                prev.copy(
                    actions = emptyList()
                )
            }
            return
        }

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

    }

    private fun handleQrCodeForAccountManagement(
        personGuid: String,
        url: String,
    ) {
        // For account management, we need the school URL from route
        val schoolUrl = route.schoolUrl ?: throw IllegalArgumentException(
            "School URL is required for account management"
        )

        // Validate the QR code with school context
        val validationResult = validateQrCodeWithSchoolContext(url, schoolUrl)

        if (validationResult.errorMessage != null) {
            _uiState.update {
                it.copy(
                    errorMessage = validationResult.errorMessage.asUiText(),
                    showManualEntryDialog = false
                )
            }
            _appUiState.update { prev ->
                prev.copy(
                    actions = emptyList()
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    errorMessage = null,
                    showManualEntryDialog = false
                )
            }

            if (
                //If sending result using nav result returner, then return/pop as normal
                !resultReturner.sendResultIfResultExpected(
                    route = route,
                    navCommandFlow = _navCommandFlow,
                    result = url,
                )
            ) {
                // Else - user is coming from CreateAccountSetUserName and needs to go forward to
                // ManageAccount
                _navCommandFlow.tryEmit(
                    NavCommand.Navigate(
                        destination = ManageAccount(
                            guid = personGuid,
                            qrUrlStr = url,
                            username = route.username
                        ),
                        popUpToClass = CreateAccountSetUsername::class,
                        popUpToInclusive = true
                    )
                )
                _uiState.update { it.copy(showManualEntryDialog = false) }
            }
        }
    }

    fun onClickTryAgain() {
        _uiState.update { currentState ->
            currentState.copy(errorMessage = null)
        }

        // Restore the action buttons when error is cleared
        _appUiState.update { prev ->
            prev.copy(
                actions = listOf(
                    AppActionButton(
                        icon = AppStateIcon.MORE_VERT,
                        contentDescription = Res.string.more_options.asUiText(),
                        text = Res.string.paste_url.asUiText(),
                        onClick = {
                            _uiState.update { currentState ->
                                currentState.copy(showManualEntryDialog = true)
                            }
                        },
                        id = "more_options_qr_scan",
                        display = AppActionButton.Companion.ActionButtonDisplay.OVERFLOW_MENU
                    )
                )
            )
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