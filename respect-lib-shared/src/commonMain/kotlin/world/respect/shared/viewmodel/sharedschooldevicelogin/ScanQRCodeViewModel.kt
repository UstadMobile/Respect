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
import world.respect.shared.generated.resources.scan_qr_code
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
import world.respect.shared.viewmodel.app.appstate.Snack
import world.respect.shared.viewmodel.app.appstate.SnackBarDispatcher

data class ScanQRCodeUiState(
    val qrCodeUrl: String = "",
    val isLoading: Boolean = false,
    val errorMessage: UiText? = null,
    val isSuccess: Boolean = false,
    val showPasteButton: Boolean = false,
    val loginErrorText: UiText? = null,
    val manualUrlError: UiText? = null,
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
                showMoreIconVisible = true,
                onClickMoreOption = {
                    _uiState.update { it.copy(showPasteButton = true) }
                },
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
                    showPasteButton = false,
                    manualUrlError = null
                )
            }
            try {
                if (route.resultDest != null) {
                    storeQrCodeForPerson(route.guid ?: "", url) // ASSIGNING
                } else {
                    authenticateWithQrCode(url)  // LOGIN
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
        return try {
            validateQrCodeUseCase.validateFormatOnly(
                qrCodeUrl = url,
                schoolUrl = route.schoolUrl?.toString()
            )
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    manualUrlError = e.getUiTextOrGeneric()
                )
            }
            false
        }
    }

    private fun authenticateWithQrCode(url: String) {
        launchWithLoadingIndicator {
            try {
                // Validate QR code format for login
                val credential = RespectQRBadgeCredential(qrCodeUrl = Url(url))
                val qrCodeUrl = credential.qrCodeUrl
                val schoolUrlString = buildString {
                    append(qrCodeUrl.protocol.name)
                    append("://")
                    append(qrCodeUrl.host)
                    if (qrCodeUrl.port != qrCodeUrl.protocol.defaultPort) {
                        append(":")
                        append(qrCodeUrl.port)
                    }
                    append("/")
                }
                validateQrCodeUseCase(
                    qrCodeUrl = url,
                    schoolUrl = schoolUrlString,
                    personGuid = null,
                    allowReplacement = false
                )

                println(route.schoolUrl)

                val schoolUrlObject = Url(schoolUrlString)
                val schoolScopeId = SchoolDirectoryEntryScopeId(schoolUrlObject, null)
                val schoolScope =
                    getKoin().getOrCreateScope<SchoolDirectoryEntry>(schoolScopeId.scopeId)

                val respectAccountManager: RespectAccountManager = schoolScope.get()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isSuccess = true,
                        showPasteButton = false
                    )
                }
                viewModelScope.launch {
                    try {
                        respectAccountManager.login(
                            credential = credential,
                            schoolUrl = schoolUrlObject
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
                throw e
            }
        }
    }

    private fun storeQrCodeForPerson(personGuid: String, url: String) {
        viewModelScope.launch {
            try {
                // Validate QR code before storing
                validateQrCodeUseCase(
                    qrCodeUrl = url,
                    schoolUrl = route.schoolUrl?.toString(),
                    personGuid = personGuid,
                    allowReplacement = true
                )

                // If validation passes, send the result
                if (
                    !resultReturner.sendResultIfResultExpected(
                        route = route,
                        navCommandFlow = _navCommandFlow,
                        result = url,
                    )
                ) {
                    _navCommandFlow.tryEmit(
                        NavCommand.Navigate(ManageAccount(personGuid))
                    )
                }
                _uiState.update { it.copy(showPasteButton = false) }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.getUiTextOrGeneric(),
                        showPasteButton = false
                    )
                }
                snackBarDispatcher.showSnackBar(Snack(e.getUiTextOrGeneric()))
                throw e
            }
        }
    }
}