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
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.account.validateqrbadge.ValidateQrCodeUseCase
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.paste_url
import world.respect.shared.generated.resources.scan_qr_code
import world.respect.shared.navigation.ManageAccount
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.NavResultReturner
import world.respect.shared.navigation.PersonDetail
import world.respect.shared.navigation.RespectAppLauncher
import world.respect.shared.navigation.ScanQRCode
import world.respect.shared.navigation.sendResultIfResultExpected
import world.respect.shared.resources.UiText
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
    private val validateQrCodeUseCase: ValidateQrCodeUseCase
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
                        icon = AppStateIcon.MORE_VERT, // You'll need to define this or use appropriate icon
                        contentDescription = "more_options",
                        text = Res.string.paste_url.asUiText(), // Text for overflow menu
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
                    handleQrCodeForAccountManagement(route.guid ?: "", url) // ASSIGNING
                } else {
                    authenticateWithQrCode(Url(url))  // LOGIN
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

    fun validateUrl(url: String): Boolean {
        _uiState.update {
            it.copy(manualUrlError = null)
        }
        if (url.isBlank()) {
            return false
        }

        val validationResult = validateQrCodeUseCase.validateFormatOnly(
            qrCodeUrl = url,
            schoolUrl = route.schoolUrl?.toString()
        )

        return if (validationResult.isValid) {
            true
        } else {
            _uiState.update {
                it.copy(
                    manualUrlError = validationResult.errorMessage?.asUiText()
                )
            }
            false
        }
    }

    fun hideManualEntryDialog() {
        _uiState.update { currentState ->
            currentState.copy(showManualEntryDialog = false)
        }
    }

    private fun authenticateWithQrCode(url: Url) {
        launchWithLoadingIndicator {
            try {
                // Validate QR code format for login
                val credential = RespectQRBadgeCredential(qrCodeUrl = url)

                val schoolUrl = credential.extractSchoolUrl()
                    ?: throw IllegalArgumentException("Invalid QR code format: Unable to extract school URL")

                val qrBadgeValidation = validateQrCodeUseCase(
                    qrCodeUrl = url.toString(),
                    schoolUrl = schoolUrl.toString(),
                    personGuid = null,
                    allowReplacement = false
                )
                _uiState.update {
                    it.copy(
                        manualUrlError = qrBadgeValidation.errorMessage?.asUiText()
                    )
                }

                if (qrBadgeValidation.errorMessage == null) {

                    val respectAccountManager: RespectAccountManager = getKoin().get()

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSuccess = true,
                        )
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
                }
            } catch (e: Exception) {
                throw e
            }
        }
    }

    fun clearValidationError() {
        _uiState.update {
            it.copy(manualUrlError = null)
        }
    }

    private fun handleQrCodeForAccountManagement(personGuid: String, url: String) {
        viewModelScope.launch {
            try {
                val qrBadgeValidation = validateQrCodeUseCase(
                    qrCodeUrl = url,
                    schoolUrl = route.schoolUrl?.toString(),
                    personGuid = null,
                    allowReplacement = false
                )
                _uiState.update {
                    it.copy(
                        manualUrlError = qrBadgeValidation.errorMessage?.asUiText()
                    )
                }

                if (qrBadgeValidation.errorMessage == null) {

                    if (route.resultDest != null) {
                        // Coming from ManageAccount - use result pattern
                        resultReturner.sendResultIfResultExpected(
                            route = route,
                            navCommandFlow = _navCommandFlow,
                            result = url,
                        )
                    } else {
                        // Coming from SetUsernameAndPassword/Create Account Screen - navigate directly
                        _navCommandFlow.tryEmit(
                            NavCommand.Navigate(
                                destination = ManageAccount(
                                    guid = personGuid,
                                    qrUrlStr = url,
                                    username = route.username
                                ),
                                popUpToClass = PersonDetail::class,
                                popUpToInclusive = false
                            )
                        )
                    }
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.getUiTextOrGeneric(),
                    )
                }
                snackBarDispatcher.showSnackBar(Snack(e.getUiTextOrGeneric()))
                throw e
            }
        }
    }
}