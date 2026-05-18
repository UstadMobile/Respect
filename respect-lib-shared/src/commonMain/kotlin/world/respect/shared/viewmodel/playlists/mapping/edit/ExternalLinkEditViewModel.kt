package world.respect.shared.viewmodel.playlists.mapping.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import world.respect.lib.opds.model.OpdsPublication
import world.respect.lib.opds.model.ReadiumMetadata
import world.respect.lib.opds.model.ReadiumLink
import world.respect.lib.opds.model.LangMapStringValue
import world.respect.shared.domain.externallink.ExtractWebPageMetadataUseCase
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.done
import world.respect.shared.generated.resources.external_link
import world.respect.shared.generated.resources.required_field
import world.respect.shared.navigation.ExternalLinkEdit
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.NavResult
import world.respect.shared.navigation.NavResultReturner
import world.respect.shared.navigation.RouteResultDest
import world.respect.shared.resources.UiText
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.ActionBarButtonUiState

data class ExternalLinkUiState(
    val url: String = "",
    val title: String = "",
    val description: String = "",
    val imageUrl: String? = null,
    val urlError: UiText? = null,
    val titleError: UiText? = null,
    val step: Step = Step.URL,
    val isLoading: Boolean = false,
) {
    enum class Step {
        URL,
        METADATA
    }
}

class ExternalLinkViewModel(
    savedStateHandle: SavedStateHandle,
    private val resultReturner: NavResultReturner,
    private val extractWebPageMetadataUseCase: ExtractWebPageMetadataUseCase,
) : RespectViewModel(savedStateHandle) {

    private val route: ExternalLinkEdit = savedStateHandle.toRoute()

    private val _uiState = MutableStateFlow(ExternalLinkUiState())
    val uiState = _uiState.asStateFlow()

    init {
        _appUiState.update { prev ->
            prev.copy(
                title = Res.string.external_link.asUiText(),
                userAccountIconVisible = false,
                hideBottomNavigation = true,
                actionBarButtonState = ActionBarButtonUiState(
                    visible = false,
                )
            )
        }
    }


    fun onUrlChanged(url: String) {
        _uiState.update {
            it.copy(
                url = url,
                urlError = null
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
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val metadata = extractWebPageMetadataUseCase(url)
                _uiState.update {
                    it.copy(
                        step = ExternalLinkUiState.Step.METADATA,
                        title = metadata.title ?: "",
                        description = metadata.description ?: "",
                        imageUrl = metadata.imageUrl,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        step = ExternalLinkUiState.Step.METADATA,
                        isLoading = false
                    )
                }
            }
            _appUiState.update { prev ->
                prev.copy(
                    actionBarButtonState = ActionBarButtonUiState(
                        visible = true,
                        text = Res.string.done.asUiText(),
                        onClick = ::onClickDone
                    )
                )
            }
        }
    }
    fun onTitleChanged(title: String) {
        _uiState.update {
            it.copy(
                title = title,
                titleError = null
            )
        }
    }

    fun onDescriptionChanged(description: String) {
        _uiState.update {
            it.copy(description = description)
        }
    }

    fun onClickDone() {
        val state = _uiState.value

        if (state.title.isBlank()) {
            _uiState.update {
                it.copy(titleError = Res.string.required_field.asUiText())
            }
            return
        }
        val publication = OpdsPublication(
            metadata = ReadiumMetadata(
                title = LangMapStringValue(state.title.trim()),
                description = state.description.trim().takeIf { it.isNotBlank() },
            ),
            links = listOf(
                ReadiumLink(
                    href = state.url,
                    rel = listOf("self"),
                    type = "text/html",
                )
            ),
            images = state.imageUrl?.let {
                listOf(ReadiumLink(href = it, type = "image/*"))
            }
        )

        val resultDest = route.resultDest
            ?: throw IllegalStateException("resultDest is null")

        resultReturner.sendResult(
            NavResult(
                key = resultDest.resultKey,
                result = publication,
            )
        )

        _navCommandFlow.tryEmit(
            NavCommand.PopToRoute(
                destination = (resultDest as RouteResultDest).resultPopUpTo,
                inclusive = false,
            )
        )
    }
}