package world.respect.shared.viewmodel.onboarding

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.viewmodel.RespectViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import world.respect.shared.navigation.GetStartedScreen
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.RespectAppLauncher

data class OnboardingUiState(
    val isLoading: Boolean = false
)

class OnboardingViewModel(
    savedStateHandle: SavedStateHandle,
    private val accountManager: RespectAccountManager
) : RespectViewModel(savedStateHandle) {

    private val _uiState = MutableStateFlow(OnboardingUiState())

    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _appUiState.update { prev ->
                prev.copy(
                    hideBottomNavigation = true,
                    hideAppBar = true
                )
            }
        }
    }
    fun onClickGetStartedButton(){
            _navCommandFlow.tryEmit(
                NavCommand.Navigate(
                    destination = if(accountManager.selectedAccount != null) {
                        RespectAppLauncher
                    }else {
                        GetStartedScreen
                    },
                )
            )
    }
}