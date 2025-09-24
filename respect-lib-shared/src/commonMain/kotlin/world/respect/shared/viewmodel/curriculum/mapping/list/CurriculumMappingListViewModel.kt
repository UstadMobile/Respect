package world.respect.shared.viewmodel.curriculum.mapping.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import world.respect.datalayer.db.curriculum.entities.TextbookMapping
import world.respect.shared.domain.curriculum.mapping.GetCurriculumMappingsUseCase
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.map
import world.respect.shared.generated.resources.mapping
import world.respect.shared.navigation.CurriculumMappingEdit
import world.respect.shared.navigation.NavCommand
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.FabUiState

data class CurriculumMappingListUiState(
    val textbooks: List<TextbookMapping> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null,
)

class CurriculumMappingListViewModel(
    savedStateHandle: SavedStateHandle,
    private val getCurriculumMappingsUseCase: GetCurriculumMappingsUseCase,
) : RespectViewModel(savedStateHandle) {

    private val _uiState = MutableStateFlow(CurriculumMappingListUiState())
    val uiState = _uiState.asStateFlow()

    init {
        _appUiState.update { prev ->
            prev.copy(
                title = Res.string.mapping.asUiText(),
                userAccountIconVisible = true,
                fabState = FabUiState(
                    visible = true,
                    text = Res.string.map.asUiText(),
                    icon = FabUiState.FabIcon.ADD,
                    onClick = ::onClickMap
                ),
                hideBottomNavigation = false,
            )
        }

        loadTextbooks()
    }

    private fun loadTextbooks() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }

            getCurriculumMappingsUseCase.getTextbooks()
                .catch { throwable ->
                    _uiState.update {
                        it.copy(
                            loading = false,
                            error = throwable.message ?: "Unknown error occurred"
                        )
                    }
                }
                .collect { textbooks ->
                    _uiState.update {
                        it.copy(
                            textbooks = textbooks,
                            loading = false,
                            error = null
                        )
                    }
                }
        }
    }

    fun onClickTextbook(textbook: TextbookMapping) {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(CurriculumMappingEdit(textbook.uid))
        )
    }

    fun onClickMoreOptions(textbook: TextbookMapping) {
        // TODO:
    }

    fun onClickMap() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(CurriculumMappingEdit(NEW_TEXTBOOK_UID))
        )
    }

    fun onRetry() {
        loadTextbooks()
    }

    companion object {
        private const val NEW_TEXTBOOK_UID = 0L
    }
}