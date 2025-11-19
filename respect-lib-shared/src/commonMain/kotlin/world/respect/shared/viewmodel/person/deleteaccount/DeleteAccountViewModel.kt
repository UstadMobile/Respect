package world.respect.shared.viewmodel.person.deleteaccount

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
import world.respect.shared.domain.account.deleteaccount.DeleteAccountUseCase
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.delete_account
import world.respect.shared.generated.resources.error_name_mismatched
import world.respect.shared.generated.resources.required
import world.respect.shared.navigation.DeleteAccount
import world.respect.shared.navigation.GetStartedScreen
import world.respect.shared.navigation.NavCommand
import world.respect.shared.resources.UiText
import world.respect.shared.util.ext.asUiText
import world.respect.shared.util.ext.fullName
import world.respect.shared.viewmodel.RespectViewModel
import kotlin.getValue

data class DeleteAccountUiState(
    val userName: String? = null,
    val enteredName: String = "",
    val userNameError: UiText? = null,
)

class DeleteAccountViewModel(
    savedStateHandle: SavedStateHandle,
    private val accountManager: RespectAccountManager,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireSelectedAccountScope()

    private val route: DeleteAccount = savedStateHandle.toRoute()

    private val deleteAccountUseCase: DeleteAccountUseCase = scope.get()

    private val _uiState = MutableStateFlow(DeleteAccountUiState())

    val uiState = _uiState.asStateFlow()

    private val schoolDataSource: SchoolDataSource by inject()

    init {
        viewModelScope.launch {

            schoolDataSource.personDataSource.findByGuidAsFlow(
                guid = route.guid,
            ).collect { person ->
                val personSelected = person.dataOrNull()

                _uiState.update { prev ->
                    prev.copy(
                        userName = personSelected?.fullName(),
                        enteredName = personSelected?.fullName().orEmpty()
                    )
                }
            }


        }
        _appUiState.update {
            it.copy(
                title = Res.string.delete_account.asUiText()
            )
        }
    }

    fun onEntityChanged(username: String) {
        _uiState.update { current ->
            val actualName = current.userName.orEmpty()
            val isMismatch = username != actualName

            current.copy(
                enteredName = username,
                userNameError = when {
                    username.isBlank() -> Res.string.required.asUiText()
                    isMismatch -> Res.string.error_name_mismatched.asUiText()
                    else -> null
                }
            )
        }
    }

    fun onDeleteAccount() {
        viewModelScope.launch {
            try {
                deleteAccountUseCase()

                val account = accountManager.selectedAccount
                if (account != null) {
                    accountManager.endSession(account)
                }

                _navCommandFlow.tryEmit(
                    NavCommand.Navigate(
                        destination = GetStartedScreen(),
                        clearBackStack = true
                    )
                )

            } catch (e: Exception) {
                e.printStackTrace()
                println("Delete failed due to exception: ${e.message}")
            }
        }
    }
}