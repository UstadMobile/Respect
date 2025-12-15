package world.respect.shared.viewmodel.person.setusernameandpassword

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.ext.dataOrNull
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.account.username.filterusername.FilterUsernameUseCase
import world.respect.shared.domain.account.username.validateusername.ValidateUsernameUseCase
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.create_account
import world.respect.shared.generated.resources.password_not_set_error
import world.respect.shared.generated.resources.save
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.NavResultReturner
import world.respect.shared.navigation.ScanQRCode
import world.respect.shared.navigation.SetPassword
import world.respect.shared.navigation.SetUsernameAndPassword
import world.respect.shared.resources.UiText
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.ActionBarButtonUiState
import kotlin.time.Clock

data class SetUsernameAndPasswordUiState(
    val username: String = "",
    val usernameErr: UiText? = null,
    val passwordErr: UiText? = null,
    val isPasswordSet: Boolean = false,
)

/**
 * Used to manually set a username and password for an existing person where the active user (eg.
 * admin) has sufficient permission to do so.
 */
class SetUsernameAndPasswordViewModel(
    savedStateHandle: SavedStateHandle,
    accountManager: RespectAccountManager,
    private val filterUsernameUseCase: FilterUsernameUseCase,
    private val validateUsernameUseCase: ValidateUsernameUseCase,
): RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireSelectedAccountScope()

    private val schoolDataSource: SchoolDataSource by inject()
    private val navResultReturner: NavResultReturner by inject()

    private val route: SetUsernameAndPassword = savedStateHandle.toRoute()

    private val _uiState = MutableStateFlow(SetUsernameAndPasswordUiState())

    val uiState = _uiState.asStateFlow()

    companion object {
        const val PASSWORD_SET_RESULT = "password_set_result"
    }

    init {
        _appUiState.update {
            it.copy(
                title = Res.string.create_account.asUiText(),
                hideBottomNavigation = true,
                actionBarButtonState = ActionBarButtonUiState(
                    text = Res.string.save.asUiText(),
                    visible = true,
                    onClick = ::onClickSave
                )
            )
        }

        viewModelScope.launch {
            navResultReturner.filteredResultFlowForKey(PASSWORD_SET_RESULT)
                .collectLatest { navResult ->
                    val passwordWasSet = navResult.result as? Boolean ?: return@collectLatest

                    if (passwordWasSet) {
                        _uiState.update {
                            it.copy(
                                isPasswordSet = true,
                                passwordErr = null
                            )
                        }
                        Napier.d("Password set result received")
                    }
                }
        }
    }

    fun onUsernameChanged(username: String) {
        _uiState.update { it.copy(username = filterUsernameUseCase(username, "")) }
    }

    fun onAssignQrCodeBadge() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(ScanQRCode(guid = route.guid))
        )
    }

    fun onSetPassword() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(SetPassword(route.guid))
        )
    }

    fun onLearnMore() {
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

                if(usernameValidation.errorMessage != null)
                    return@launchWithLoadingIndicator


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
                _navCommandFlow.tryEmit(NavCommand.PopUp())
            }catch (e: Throwable) {
                Napier.e("Error saving username and password ${e.message}", e)
            }
        }
    }
}