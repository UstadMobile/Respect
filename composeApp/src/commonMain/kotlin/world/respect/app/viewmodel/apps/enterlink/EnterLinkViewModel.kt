package world.respect.app.viewmodel.apps.enterlink

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import respect.composeapp.generated.resources.Res
import respect.composeapp.generated.resources.enter_link
import world.respect.app.viewmodel.RespectViewModel

data class EnterLinkUiState(
    val reportData: List<String> = emptyList(),
    val isError: Boolean = false,
)

class EnterLinkViewModel(
    savedStateHandle: SavedStateHandle,
) : RespectViewModel(savedStateHandle) {

    private val _uiState = MutableStateFlow(EnterLinkUiState())
    val uiState = _uiState.asStateFlow()


    init {
        viewModelScope.launch {
            _appUiState.update {
                it.copy(
                    title = getString(Res.string.enter_link)
                )
            }

        }
    }
    fun isValidUrl(link: String): Boolean {
        return true
    }
    fun onButtonClick(link: String):Boolean{
        val isValid = isValidUrl(link)
        _uiState.update {
            it.copy(
                isError = !isValid,
            )
        }
        return isValid
    }

}
