package world.respect.shared.viewmodel.testing

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import world.respect.shared.domain.testing.SendDbToServerUseCase
import world.respect.shared.navigation.SendDbToServer
import world.respect.shared.viewmodel.RespectViewModel

data class SendDbToServerUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

class SendDbToServerViewModel(
    savedStateHandle: SavedStateHandle,
    private val sendDbToServerUseCase: SendDbToServerUseCase,
) : RespectViewModel(savedStateHandle) {

    private val route: SendDbToServer = savedStateHandle.toRoute()

    private val _uiState = MutableStateFlow(SendDbToServerUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                sendDbToServerUseCase(schoolUrl = route.schoolUrl)
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message,
                    )
                }
            }
        }
    }
}
