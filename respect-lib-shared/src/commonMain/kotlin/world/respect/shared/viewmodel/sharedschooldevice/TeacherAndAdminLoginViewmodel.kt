package world.respect.shared.viewmodel.sharedschooldevice

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import world.respect.datalayer.RespectAppDataSource
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.teacher_admin_login
import world.respect.shared.navigation.LoginScreen
import world.respect.shared.navigation.NavCommand
import world.respect.shared.resources.UiText
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel

data class TeacherAndAdminLoginUiState(
    val errorMessage: UiText? = null,
    val pin: String = "",
)

class TeacherAndAdminLoginViewmodel(
    savedStateHandle: SavedStateHandle,
    private val accountManager: RespectAccountManager,
    private val respectAppDataSource: RespectAppDataSource
) : RespectViewModel(savedStateHandle) {

    private val _uiState = MutableStateFlow(TeacherAndAdminLoginUiState())

    val uiState = _uiState.asStateFlow()


    init {
        _appUiState.update {
            it.copy(
                title = Res.string.teacher_admin_login.asUiText(),
                hideBottomNavigation = true,
                userAccountIconVisible = false
            )
        }
    }

    fun onPinChanged(pin: String) {
        _uiState.update { it.copy(pin = pin) }
    }

    fun onClickNext() {
        viewModelScope.launch {
            val schoolUrl = accountManager.activeAccount?.school?.self
            val currentAccounts = accountManager.accounts.value
            currentAccounts.forEach { account ->
                accountManager.removeAccount(account)

            }
            schoolUrl?.let { url ->
                _navCommandFlow.tryEmit(
                    NavCommand.Navigate(LoginScreen.create(url,true))
                )
            }
        }
    }

    fun verifyTeacherPin(enteredPin: String): Boolean {
        // TODO
        return true
    }
}