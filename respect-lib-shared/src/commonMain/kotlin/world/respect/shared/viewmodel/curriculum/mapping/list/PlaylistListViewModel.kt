package world.respect.shared.viewmodel.curriculum.mapping.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import world.respect.shared.navigation.CurriculumMappingEdit
import world.respect.shared.navigation.EnterLink
import world.respect.shared.navigation.LearningUnitDetail
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.NavResultReturner
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.curriculum.mapping.edit.CurriculumMappingEditViewModel
import world.respect.shared.viewmodel.curriculum.mapping.model.CurriculumMapping
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.error_unexpected_result_type
import world.respect.shared.resources.UiText

data class PlaylistListUiState(
    val mappings: List<CurriculumMapping> = emptyList(),
    val selectedFilterIndex: Int = 0,
    val error: UiText? = null,
)

class PlaylistListViewModel(
    savedStateHandle: SavedStateHandle,
    private val resultReturner: NavResultReturner,
) : RespectViewModel(savedStateHandle) {

    private val _uiState = MutableStateFlow(PlaylistListUiState())

    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            resultReturner.filteredResultFlowForKey(
                CurriculumMappingEditViewModel.KEY_SAVED_MAPPING
            ).collect { result ->
                val savedMapping = result.result as? CurriculumMapping
                if (savedMapping == null) {
                    _uiState.update {
                        it.copy(error = Res.string.error_unexpected_result_type.asUiText())
                    }
                    return@collect
                }
                addOrUpdateMapping(savedMapping)
            }
        }
    }

    fun setMappings(mappings: List<CurriculumMapping>) {
        _uiState.update { it.copy(mappings = mappings) }
    }

    fun onFilterSelected(index: Int) {
        _uiState.update { it.copy(selectedFilterIndex = index) }
    }

    fun onClickMapping(mapping: CurriculumMapping) {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                LearningUnitDetail.createFromMapping(mapping)
            )
        )
    }

    fun onClickAddNew() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                CurriculumMappingEdit.create(uid = 0L, mappingData = null)
            )
        )
    }

    fun onClickAddLink() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                EnterLink.create()
            )
        )
    }

    fun removeMapping(mapping: CurriculumMapping): List<CurriculumMapping> {
        val updated = _uiState.value.mappings.filter { it.uid != mapping.uid }
        _uiState.update { it.copy(mappings = updated) }
        return updated
    }

    private fun addOrUpdateMapping(mapping: CurriculumMapping) {
        val currentMappings = _uiState.value.mappings.toMutableList()
        val existingIndex = currentMappings.indexOfFirst { it.uid == mapping.uid }

        if (existingIndex >= 0) {
            currentMappings[existingIndex] = mapping
        } else {
            val newMapping = if (mapping.uid == 0L) {
                mapping.copy(uid = System.currentTimeMillis())
            } else {
                mapping
            }
            currentMappings.add(newMapping)
        }

        _uiState.update { it.copy(mappings = currentMappings) }
    }
}