package world.respect.shared.viewmodel.person.setusernameandpassword

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.ext.dataOrNull
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.account.setpassword.EncryptPersonPasswordUseCase
import world.respect.shared.domain.account.username.filterusername.FilterUsernameUseCase
import world.respect.shared.domain.account.username.validateusername.ValidateUsernameUseCase
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.create_account
import world.respect.shared.generated.resources.password_must_be_at_least
import world.respect.shared.generated.resources.required_field
import world.respect.shared.generated.resources.save
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.ScanQRCode
import world.respect.shared.navigation.SetPassword
import world.respect.shared.navigation.SetUsernameAndPassword
import world.respect.shared.resources.UiText
import world.respect.shared.util.ext.asUiText
import world.respect.shared.util.ext.isStudent
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.ActionBarButtonUiState
import world.respect.shared.viewmodel.person.setusernameandpassword.CreateAccountSetPasswordViewModel.Companion.MIN_PASSWORD_LENGTH
import kotlin.time.Clock

data class SetUsernameAndPasswordUiState(
    val username: String = "",
    val usernameErr: UiText? = null,
    val passwordErr: UiText? = null,
    val password: String = "",
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
    private val encryptPersonPasswordUseCase: EncryptPersonPasswordUseCase,
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
            ).collect { personResult ->
                val person = personResult.dataOrNull()
                val isStudent = person?.isStudent() == true

                _uiState.update { prev ->
                    prev.copy(
                        isStudent = isStudent
                    )
                }
                _appUiState.update { appUiState ->
                    if (!isStudent) {
                        appUiState.copy(
                            actionBarButtonState = ActionBarButtonUiState(
                                text = Res.string.save.asUiText(),
                                visible = true,
                                onClick = ::onClickSave
                            )
                        )
                    } else {
                        appUiState.copy(
                            actionBarButtonState = ActionBarButtonUiState(
                                visible = false
                            )
                        )
                    }
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

    private fun validatePassword(): UiText? {
        return when {
            uiState.value.password.isEmpty() -> Res.string.required_field.asUiText()
            uiState.value.password.length < MIN_PASSWORD_LENGTH -> Res.string.password_must_be_at_least.asUiText()
            else -> null
        }
    }

    fun onClickSave() {
        launchWithLoadingIndicator {
            try {
                val username = uiState.value.username.trim()
                val usernameValidation = validateUsernameUseCase(username)
                _uiState.update {
                    it.copy(
                        usernameErr = usernameValidation.errorMessage?.asUiText()
                    )
                }

                if (usernameValidation.errorMessage != null)
                    return@launchWithLoadingIndicator

                val error = validatePassword()

                if (error != null) {
                    _uiState.update { it.copy(passwordErr = error) }
                    return@launchWithLoadingIndicator
                }

                val person = schoolDataSource.personDataSource.findByGuid(
                    DataLoadParams(), route.guid
                ).dataOrNull() ?: throw IllegalStateException()

                schoolDataSource.personDataSource.store(
                    listOf(
                        person.copy(
                            username = uiState.value.username,
                            lastModified = Clock.System.now(),
                        )
                    )
                )

                schoolDataSource.personPasswordDataSource.store(
                    listOf(
                        encryptPersonPasswordUseCase(
                            EncryptPersonPasswordUseCase.Request(
                                personGuid = route.guid,
                                password = uiState.value.password
                            )
                        )
                    )
                )

                _navCommandFlow.tryEmit(NavCommand.PopUp())
            } catch (e: Throwable) {
                Napier.e("Error saving username and password", e)
            }

        }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(password = password) }
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
                        ),
                    )
                )
            }
        }
    }

    fun onLearnMore() {
        //TODO
    }
}