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
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.credentials.passkey.RespectQRBadgeCredential
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.school.model.PersonQrCode
import world.respect.libutil.util.throwable.ForbiddenException
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.account.authenticatepassword.AuthenticateQrBadgeUseCase
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.saved
import world.respect.shared.generated.resources.scan_qr_code
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.ScanQRCode
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.Snack
import world.respect.shared.viewmodel.app.appstate.SnackBarDispatcher
import kotlin.time.Clock

data class ScanQRCodeUiState(
    val qrCodeUrl: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

class ScanQRCodeViewModel(
    savedStateHandle: SavedStateHandle,
    accountManager: RespectAccountManager,
    private val snackBarDispatcher: SnackBarDispatcher,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireSelectedAccountScope()

    private val _uiState = MutableStateFlow(ScanQRCodeUiState())
    val uiState: Flow<ScanQRCodeUiState> = _uiState.asStateFlow()

    private val route: ScanQRCode = savedStateHandle.toRoute()

    private val schoolDataSource: SchoolDataSource by inject()
    private val authenticateQrBadgeUseCase: AuthenticateQrBadgeUseCase by inject()

    init {
        _appUiState.update { prev ->
            prev.copy(
                title = Res.string.scan_qr_code.asUiText(),
                navigationVisible = true,
                hideBottomNavigation = true,
                settingsIconVisible = false
            )
        }
    }

    fun processQrCodeUrl(url: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                // Check if we're in "store mode" or "login mode"
                val personGuid = route.guid

                if (personGuid.isNullOrEmpty()) {
                    // LOGIN MODE: Authenticate using the QR code
                    authenticateWithQrCode(url)
                } else {
                    // STORE MODE: Save QR code for the specific person
                    storeQrCodeForPerson(personGuid, url)
                }
            } catch (e: Exception) {
                Napier.e("Error processing QR Code", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Failed to process QR code"
                    )
                }
                snackBarDispatcher.showSnackBar(Snack("Failed to process QR code".asUiText()))
            }
        }
    }

    private suspend fun authenticateWithQrCode(url: String) {
        try {
            // Create credential from scanned URL
            val credential = RespectQRBadgeCredential(qrCodeUrl = Url(url))

            // Authenticate using the use case
            val response = authenticateQrBadgeUseCase(credential)

            // Authentication successful
            _uiState.update { it.copy(isLoading = false, isSuccess = true) }

            // Navigate back or to next screen
            _navCommandFlow.tryEmit(NavCommand.PopUp())

            // Show success message
            snackBarDispatcher.showSnackBar(Snack("Authentication successful".asUiText()))

        } catch (e: ForbiddenException) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = "Invalid QR code. Please try again."
                )
            }
            snackBarDispatcher.showSnackBar(Snack("Invalid QR code. Please try again".asUiText()))
        } catch (e: Exception) {
            throw e // Re-throw to be handled by the outer catch
        }
    }

    private suspend fun storeQrCodeForPerson(personGuid: String, url: String) {
        try {
            val now = Clock.System.now()
            schoolDataSource.personQrDataSource.store(
                listOf(
                    PersonQrCode(
                        personGuid = personGuid,
                        qrCodeUrl = url,
                        lastModified = now,
                        stored = now
                    )
                )
            )

            _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            _navCommandFlow.tryEmit(NavCommand.PopUp())

            snackBarDispatcher.showSnackBar(Snack("QR code saved successfully".asUiText()))
        } catch (e: Exception) {
            throw e // Re-throw to be handled by the outer catch
        }
    }
}