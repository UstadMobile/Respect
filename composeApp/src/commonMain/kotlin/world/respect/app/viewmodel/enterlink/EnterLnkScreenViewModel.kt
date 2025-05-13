package world.respect.app.viewmodel.enterlink

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
)

class EnterLnkScreenViewModel(
) : RespectViewModel() {

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
    //added mock regex just for test purpose
    fun isValidUrl(url: String): Boolean {
        val regex = Regex(
            "^https?://[\\w.-]+(?:\\.[\\w.-]+)+[/\\w\\-._~:?#[\\\\]@!$&'()*+,;=.%]*$"
        )
        return url.matches(regex)
    }


}
