package world.respect.shared.viewmodel.manageuser.termsandcondition

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.terms_and_conditions
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.ProfileScreen
import world.respect.shared.navigation.TermsAndCondition
import world.respect.shared.viewmodel.RespectViewModel

data class TermsAndConditionUiState(
    val termsAndConditionText: String = "",
    val isLoading: Boolean = true
)

class TermsAndConditionViewModel(
    savedStateHandle: SavedStateHandle,
) : RespectViewModel(savedStateHandle) {
    private val route: TermsAndCondition = savedStateHandle.toRoute()

    private val _uiState = MutableStateFlow(TermsAndConditionUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _appUiState.update {
                it.copy(
                    title = getString(Res.string.terms_and_conditions),
                    hideBottomNavigation = true,
                    userAccountIconVisible = false
                )
            }


            _uiState.value = TermsAndConditionUiState(
                termsAndConditionText = "",
                isLoading = false
            )
        }
    }

    fun onAcceptClicked() {
        viewModelScope.launch {
            _navCommandFlow.tryEmit(
                NavCommand.Navigate(ProfileScreen.create(route.type))
            )
        }
    }
}
