package world.respect.shared.viewmodel.sharedschooldevicelogin

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.component.KoinScopeComponent
import org.koin.core.scope.Scope
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.enter_roll_number
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel

data class EnterRollNumberUiState(
    val loading: Boolean = false,
    val rollNumber: String = "",
    val studentName: String? = null,
    val isValid: Boolean = false,
    val errorMessage: String? = null,
    )
class EnterRollNumberViewModel (
    savedStateHandle: SavedStateHandle,
    accountManager: RespectAccountManager,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {
    override val scope: Scope = accountManager.requireSelectedAccountScope()
    private val _uiState = MutableStateFlow(EnterRollNumberUiState())
    val uiState: Flow<EnterRollNumberUiState> = _uiState.asStateFlow()
    init {
        _appUiState.update { prev ->
            prev.copy(
                title = Res.string.enter_roll_number.asUiText(),
                navigationVisible = true,
                hideBottomNavigation = true,
                settingsIconVisible = false
            )
        }
    }

    fun onRollNumberChange(rollNumber: String) {
        _uiState.update { state ->
            state.copy(
                rollNumber = rollNumber,
                isValid = rollNumber.isNotBlank(),
                errorMessage = if (rollNumber.isBlank()) "Roll number is required" else null
            )
        }
    }

    fun onNextClick() {
        // Validate and navigate
        if (_uiState.value.isValid) {
            // Proceed with login
        } else {
            _uiState.update { it.copy(
                errorMessage = "Please enter a valid roll number"
            ) }
        }
    }
}