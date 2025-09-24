package world.respect.shared.viewmodel.curriculum.mapping.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import world.respect.datalayer.db.curriculum.entities.TextbookMapping
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
)

class CurriculumMappingListViewModel(
    savedStateHandle: SavedStateHandle,
    private val json: Json,
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
            _uiState.update { it.copy(loading = true) }

            val mockTextbooks = listOf(
                TextbookMapping().apply {
                    uid = 1L
                    title = "Mathematics 5"
                    description = "Complete mathematics curriculum for grade 5 students"
                    coverImageUrl = null
                },
                TextbookMapping().apply {
                    uid = 2L
                    title = "English Literature"
                    description = "Classic literature and reading comprehension"
                    coverImageUrl = null
                }
            )

            _uiState.update {
                it.copy(
                    textbooks = mockTextbooks,
                    loading = false
                )
            }
        }
    }

    fun onClickTextbook(textbook: TextbookMapping) {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(CurriculumMappingEdit(textbook.uid))
        )
    }

    fun onClickMoreOptions(textbook: TextbookMapping) {
        // TODO
    }

    fun onClickMap() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(CurriculumMappingEdit(0L))
        )
    }
}