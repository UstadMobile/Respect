package world.respect.shared.viewmodel.person.setusernameandpassword

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.ext.dataOrNull
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.account.username.filterusername.FilterUsernameUseCase
import world.respect.shared.domain.account.username.validateusername.ValidateUsernameUseCase
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.create_account
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.ScanQRCode
import world.respect.shared.navigation.SetPassword
import world.respect.shared.navigation.SetUsernameAndPassword
import world.respect.shared.resources.UiText
import world.respect.shared.util.ext.asUiText
import world.respect.shared.util.ext.isAdminOrTeacher
import world.respect.shared.viewmodel.RespectViewModel

data class SetUsernameAndPasswordUiState(
    val username: String = "",
    val usernameErr: UiText? = null,
    val passwordErr: UiText? = null,
    val isPasswordSet: Boolean = false,
    val isQrBadgeSet: Boolean = false,
    val isStudent: Boolean = false,
    val isQrAlreadyAssigned: Boolean = false
)

/**
 * Used to manually set a username and password for an existing person where the active user (eg.
 * admin) has sufficient permission to do so.
 */
class SetUsernameAndPasswordViewModel(
    savedStateHandle: SavedStateHandle,
    private val accountManager: RespectAccountManager,
    private val filterUsernameUseCase: FilterUsernameUseCase,
    private val validateUsernameUseCase: ValidateUsernameUseCase
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()

    private val schoolDataSource: SchoolDataSource by inject()

    private val route: SetUsernameAndPassword = savedStateHandle.toRoute()

    private val _uiState = MutableStateFlow(SetUsernameAndPasswordUiState())

    val uiState = _uiState.asStateFlow()

    companion object {
        const val PASSWORD_SET_RESULT = "password_set_result"
        const val QR_SCAN_ASSIGN = "qr_scan_result"
    }

    init {
        _appUiState.update {
            it.copy(
                title = Res.string.create_account.asUiText(),
                hideBottomNavigation = true,
            )
        }

        viewModelScope.launch {
            schoolDataSource.personDataSource.findByGuidAsFlow(
                route.guid
            ).collect {
                _uiState.update { prev ->
                    prev.copy(
                        isStudent = it.dataOrNull()?.isAdminOrTeacher() == false
                    )
                }
            }
        }
        viewModelScope.launch {
            schoolDataSource.personQrDataSource.findByGuidAsFlow(
                route.guid
            ).collect {
                _uiState.update { prev ->
                    prev.copy(
                        isQrBadgeSet = it.dataOrNull() != null
                    )
                }
            }
        }
    }

    fun onUsernameChanged(username: String) {
        _uiState.update { it.copy(username = filterUsernameUseCase(username, "")) }
    }


    fun onAssignQrCodeBadge() {
        launchWithLoadingIndicator {
            val username = uiState.value.username.trim()
            val usernameValidation = validateUsernameUseCase(username)
            _uiState.update {
                it.copy(
                    usernameErr = usernameValidation.errorMessage?.asUiText()
                )
            }
            val schoolUrl = accountManager.activeAccount?.school?.self

            if (usernameValidation.errorMessage == null) {
                _navCommandFlow.tryEmit(
                    NavCommand.Navigate(
                        ScanQRCode.create(
                            username = username,
                            guid = route.guid,
                            schoolUrl = schoolUrl
                        )
                    )
                )
            }
        }
    }

    fun onSetPassword() {
        launchWithLoadingIndicator {
            val username = uiState.value.username.trim()
            val usernameValidation = validateUsernameUseCase(username)
            _uiState.update {
                it.copy(
                    usernameErr = usernameValidation.errorMessage?.asUiText()
                )
            }
            if (usernameValidation.errorMessage == null) {
                _navCommandFlow.tryEmit(
                    NavCommand.Navigate(
                        SetPassword(
                            guid = route.guid,
                            username = uiState.value.username
                        )
                    )
                )
            }
        }
    }

    fun onLearnMore() {
        //TODO
    }
}