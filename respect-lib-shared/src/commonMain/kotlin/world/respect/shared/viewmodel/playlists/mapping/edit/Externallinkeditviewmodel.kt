package world.respect.shared.viewmodel.playlists.mapping.edit

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import world.respect.lib.opds.model.ReadiumLink
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.external_link
import world.respect.shared.generated.resources.required_field
import world.respect.shared.navigation.ExternalLinkEdit
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.NavResultReturner
import world.respect.shared.resources.UiText
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel

data class ExternalLinkEditUiState(
    val url: String = "",
    val urlError: UiText? = null,
) {
    val hasErrors: Boolean
        get() = urlError != null
}

class ExternalLinkEditViewModel(
    savedStateHandle: SavedStateHandle,
    private val resultReturner: NavResultReturner,
) : RespectViewModel(savedStateHandle) {

    private val route: ExternalLinkEdit = savedStateHandle.toRoute()

    private val _uiState = MutableStateFlow(ExternalLinkEditUiState())

    val uiState = _uiState.asStateFlow()

    init {
        _appUiState.update { prev ->
            prev.copy(
                title = Res.string.external_link.asUiText(),
                userAccountIconVisible = false,
                hideBottomNavigation = true,
            )
        }
    }

    fun onUrlChanged(url: String) {
        _uiState.update {
            it.copy(
                url = url,
                urlError = null,
            )
        }
    }

    fun onClickNext() {
        val url = _uiState.value.url.trim()

        if (url.isBlank()) {
            _uiState.update {
                it.copy(urlError = Res.string.required_field.asUiText())
            }
            return
        }

        val navLink = ReadiumLink(
            href = url,
            title = url,
        )

        val resultDest = route.resultDest
            ?: throw IllegalStateException(
                "onClickNext called but resultDest is null"
            )

    }
}