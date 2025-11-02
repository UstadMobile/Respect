package world.respect.shared.viewmodel.curriculum.mapping.list

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.map
import world.respect.shared.generated.resources.mapping
import world.respect.shared.navigation.CurriculumMappingEdit
import world.respect.shared.navigation.NavCommand
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.FabUiState
import world.respect.shared.viewmodel.curriculum.mapping.model.CurriculumMapping

data class CurriculumMappingListUiState(
    val mappings: List<CurriculumMapping> = emptyList(),
)

class CurriculumMappingListViewModel(
    savedStateHandle: SavedStateHandle,
) : RespectViewModel(savedStateHandle) {

    private val _uiState = MutableStateFlow(
        CurriculumMappingListUiState(
            mappings = savedStateHandle[KEY_MAPPINGS_LIST] ?: emptyList()
        )
    )
    val uiState = _uiState.asStateFlow()

    init {
        _appUiState.update { prev ->
            prev.copy(
                title = Res.string.mapping.asUiText(),
                userAccountIconVisible = true,
                fabState = FabUiState(
                    visible = true,
                    text = Res.string.map.asUiText(),
                    onClick = ::onClickMap
                ),
                hideBottomNavigation = false,
            )
        }
    }

    fun onClickMapping(mapping: CurriculumMapping) {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(CurriculumMappingEdit(mapping.uid))
        )
    }

    fun onClickMap() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(CurriculumMappingEdit(0L))
        )
    }
    fun onClickMoreOptions(mapping: CurriculumMapping) {
        // TODO
    }

    fun updateMappings(newMappings: List<CurriculumMapping>) {
        _uiState.update { it.copy(mappings = newMappings) }
        savedStateHandle[KEY_MAPPINGS_LIST] = newMappings
    }

    fun removeMapping(mapping: CurriculumMapping) {
        val updated = _uiState.value.mappings.filter { it.uid != mapping.uid }
        updateMappings(updated)
    }

    companion object {
        private const val KEY_MAPPINGS_LIST = "mappings_list"
    }
}