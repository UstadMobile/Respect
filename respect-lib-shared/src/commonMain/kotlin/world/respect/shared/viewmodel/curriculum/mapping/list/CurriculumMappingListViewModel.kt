package world.respect.shared.viewmodel.curriculum.mapping.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.error_unexpected_result_type
import world.respect.shared.generated.resources.mapping
import world.respect.shared.generated.resources.mappings
import world.respect.shared.navigation.CurriculumMappingEdit
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.NavResultReturner
import world.respect.shared.resources.UiText
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.FabUiState
import world.respect.shared.viewmodel.curriculum.mapping.edit.CurriculumMappingEditViewModel
import world.respect.shared.viewmodel.curriculum.mapping.model.CurriculumMapping

data class CurriculumMappingListUiState(
    val mappings: List<CurriculumMapping> = emptyList(),
    val error: UiText? = null,
)

class CurriculumMappingListViewModel(
    savedStateHandle: SavedStateHandle,
    private val json: Json,
    private val resultReturner: NavResultReturner,
) : RespectViewModel(savedStateHandle) {

    private val _uiState = MutableStateFlow(
        CurriculumMappingListUiState(
            mappings = loadMappingsFromSavedState(savedStateHandle)
        )
    )
    val uiState = _uiState.asStateFlow()

    init {
        _appUiState.update { prev ->
            prev.copy(
                title = Res.string.mappings.asUiText(),
                userAccountIconVisible = true,
                fabState = FabUiState(
                    visible = true,
                    icon = FabUiState.FabIcon.ADD,
                    text = Res.string.mapping.asUiText(),
                    onClick = ::onClickMap,
                ),
                hideBottomNavigation = false,
            )
        }
        viewModelScope.launch {
            resultReturner.resultFlowForKey(
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

    private fun loadMappingsFromSavedState(savedStateHandle: SavedStateHandle): List<CurriculumMapping> {
        val mappingsJson = savedStateHandle.get<String>(KEY_MAPPINGS_LIST) ?: return emptyList()
        return try {
            json.decodeFromString<List<CurriculumMapping>>(mappingsJson)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveMappingsToSavedState(mappings: List<CurriculumMapping>) {
        savedStateHandle[KEY_MAPPINGS_LIST] = json.encodeToString(
            kotlinx.serialization.builtins.ListSerializer(CurriculumMapping.serializer()),
            mappings
        )
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

        updateMappings(currentMappings)
    }

    fun onClickMapping(mapping: CurriculumMapping) {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                CurriculumMappingEdit.create(
                    uid = mapping.uid,
                    mappingData = mapping
                )
            )
        )
    }

    fun onClickMap() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                CurriculumMappingEdit.create(uid = 0L, mappingData = null)
            )
        )
    }

    fun onClickMoreOptions(mapping: CurriculumMapping) {
        // TODO
    }

    private fun updateMappings(newMappings: List<CurriculumMapping>) {
        _uiState.update { it.copy(mappings = newMappings) }
        saveMappingsToSavedState(newMappings)
    }

    fun removeMapping(mapping: CurriculumMapping) {
        val updated = _uiState.value.mappings.filter { it.uid != mapping.uid }
        updateMappings(updated)
    }

    companion object {
        private const val KEY_MAPPINGS_LIST = "mappings_list"
    }
}