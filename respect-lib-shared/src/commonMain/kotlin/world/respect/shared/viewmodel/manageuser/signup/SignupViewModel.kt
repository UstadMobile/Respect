package world.respect.shared.viewmodel.manageuser.signup

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.create_account
import world.respect.shared.generated.resources.username_required
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.ProfileScreen
import world.respect.shared.navigation.SignupScreen
import world.respect.shared.navigation.WaitingForApproval
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.manageuser.profile.ProfileType

data class SignupUiState(
    val username: String = "",
    val usernameError: String? = null,
    val generalError: String? = null
)

class SignupViewModel(
    savedStateHandle: SavedStateHandle
) : RespectViewModel(savedStateHandle) {
    private val route: SignupScreen = savedStateHandle.toRoute()

    private val _uiState = MutableStateFlow(SignupUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _appUiState.update {
                it.copy(
                    title = getString(Res.string.create_account),
                    hideBottomNavigation = true,
                    userAccountIconVisible = false
                )
            }
        }
    }

    fun onUsernameChanged(newValue: String) {
        _uiState.update {
            it.copy(
                username = newValue,
                usernameError = null,
                generalError = null
            )
        }
    }

    fun onClickSignupWithPasskey() {
        viewModelScope.launch {
            val username = _uiState.value.username

            _uiState.update {
                it.copy(
                    usernameError = if (username.isBlank()) getString(Res.string.username_required) else null
                )
            }

            if (username.isBlank()) return@launch


            try {
                when (route.type) {
                    ProfileType.CHILD , ProfileType.Student->{
                        viewModelScope.launch {
                            _navCommandFlow.tryEmit(
                                NavCommand.Navigate(WaitingForApproval)
                            )
                        }
                    }
                    ProfileType.PARENT ->{
                        viewModelScope.launch {
                            _navCommandFlow.tryEmit(
                                NavCommand.Navigate(ProfileScreen.create(ProfileType.CHILD))
                            )
                        }
                    }
                }

            } catch (e: Exception) {

            }
        }
    }

    fun onOtherOptionsClick() {
    }
}
