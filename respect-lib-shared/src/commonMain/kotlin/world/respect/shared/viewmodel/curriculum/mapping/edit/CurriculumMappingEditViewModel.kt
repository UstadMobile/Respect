package world.respect.shared.viewmodel.curriculum.mapping.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.mapping_edit
import world.respect.shared.generated.resources.required
import world.respect.shared.generated.resources.save
import world.respect.shared.navigation.CurriculumMappingEdit
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.NavResult
import world.respect.shared.navigation.NavResultReturner
import world.respect.shared.navigation.RespectAppLauncher
import world.respect.shared.resources.UiText
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.ActionBarButtonUiState
import world.respect.shared.viewmodel.assignment.edit.AssignmentEditViewModel.Companion.KEY_LEARNING_UNIT
import world.respect.shared.viewmodel.curriculum.mapping.model.CurriculumMapping
import world.respect.shared.viewmodel.curriculum.mapping.model.CurriculumMappingSection
import world.respect.shared.viewmodel.curriculum.mapping.model.CurriculumMappingSectionLink
import world.respect.shared.viewmodel.learningunit.LearningUnitSelection

data class CurriculumMappingEditUiState(
    val mapping: CurriculumMapping? = null,
    val loading: Boolean = false,
    val isNew: Boolean = true,
    val titleError: UiText? = null,
    val error: UiText? = null,
    val pendingLessonSectionIndex: Int? = null,
) {
    val fieldsEnabled: Boolean
        get() = !loading
    val title: String
        get() = mapping?.title ?: ""
    val description: String
        get() = mapping?.description ?: ""
    val sections: List<CurriculumMappingSection>
        get() = mapping?.sections ?: emptyList()
}

class CurriculumMappingEditViewModel(
    savedStateHandle: SavedStateHandle,
    private val resultReturner: NavResultReturner,
    private val json: Json,
) : RespectViewModel(savedStateHandle) {

    private val route: CurriculumMappingEdit = savedStateHandle.toRoute()
    private val mappingUid = route.textbookUid

    private val _uiState = MutableStateFlow(
        CurriculumMappingEditUiState(
            mapping = route.mappingData ?: loadMappingFromSavedState(savedStateHandle) ?: CurriculumMapping(uid = mappingUid),
            isNew = mappingUid == 0L
        )
    )
    val uiState = _uiState.asStateFlow()

    init {
        _appUiState.update { prev ->
            prev.copy(
                title = Res.string.mapping_edit.asUiText(),
                userAccountIconVisible = false,
                actionBarButtonState = ActionBarButtonUiState(
                    visible = true,
                    text = Res.string.save.asUiText(),
                    onClick = ::onClickSave
                ),
                hideBottomNavigation = true
            )
        }

        viewModelScope.launch {
            resultReturner.filteredResultFlowForKey(KEY_LEARNING_UNIT).collect { result ->
                val learningUnit = result.result as? LearningUnitSelection ?: return@collect
                val pendingSectionIndex = _uiState.value.pendingLessonSectionIndex ?: return@collect
                val currentMapping = _uiState.value.mapping ?: return@collect
                val currentSections = currentMapping.sections.toMutableList()

                val section = currentSections.getOrNull(pendingSectionIndex) ?: return@collect

                val newLink = CurriculumMappingSectionLink(
                    href = learningUnit.learningUnitManifestUrl.toString(),
                    title = ""
                )

                currentSections[pendingSectionIndex] =
                    section.copy(items = section.items + newLink)

                updateMapping(currentMapping.copy(sections = currentSections), clearPending = true)
            }
        }
    }

    private fun loadMappingFromSavedState(savedStateHandle: SavedStateHandle): CurriculumMapping? {
        val mappingJson = savedStateHandle.get<String>(KEY_MAPPING) ?: return null
        return try {
            json.decodeFromString(CurriculumMapping.serializer(), mappingJson)
        } catch (e: Exception) {
            null
        }
    }

    private fun updateMapping(mapping: CurriculumMapping, clearPending: Boolean = false) {
        _uiState.update { prev ->
            prev.copy(
                mapping = mapping,
                pendingLessonSectionIndex = if (clearPending) null else prev.pendingLessonSectionIndex
            )
        }
        savedStateHandle[KEY_MAPPING] =
            json.encodeToString(CurriculumMapping.serializer(), mapping)
    }

    fun onTitleChanged(title: String) {
        val currentMapping = _uiState.value.mapping ?: return
        val updatedMapping = currentMapping.copy(title = title)
        updateMapping(updatedMapping)
        _uiState.update { it.copy(titleError = null) }
    }

    fun onDescriptionChanged(description: String) {
        val currentMapping = _uiState.value.mapping ?: return
        updateMapping(currentMapping.copy(description = description))
    }

    fun onClickAddSection() {
        val currentMapping = _uiState.value.mapping ?: return
        val updatedSections = currentMapping.sections + CurriculumMappingSection(title = "")
        updateMapping(currentMapping.copy(sections = updatedSections))
    }

    fun onSectionTitleChanged(sectionIndex: Int, title: String) {
        val currentMapping = _uiState.value.mapping ?: return
        val currentSections = currentMapping.sections.toMutableList()
        val section = currentSections.getOrNull(sectionIndex) ?: return
        currentSections[sectionIndex] = section.copy(title = title)
        updateMapping(currentMapping
            .copy(sections = currentSections))
    }

    fun onClickRemoveSection(sectionIndex: Int) {
        val currentMapping = _uiState.value.mapping ?: return
        val currentSections = currentMapping.sections.toMutableList()
        if (sectionIndex !in currentSections.indices) return
        currentSections.removeAt(sectionIndex)
        updateMapping(currentMapping
            .copy(sections = currentSections))
    }

    fun onSectionMoved(fromIndex: Int, toIndex: Int) {
        val currentMapping = _uiState.value.mapping ?: return
        val currentSections = currentMapping.sections.toMutableList()
        if (fromIndex !in currentSections.indices || toIndex !in currentSections.indices) return
        val section = currentSections.removeAt(fromIndex)
        currentSections.add(toIndex, section)
        updateMapping(currentMapping
            .copy(sections = currentSections))
    }

    fun onClickAddLesson(sectionIndex: Int) {
        _uiState.update { it.copy(pendingLessonSectionIndex = sectionIndex) }
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                RespectAppLauncher.create(
                    resultPopUpTo = CurriculumMappingEdit::class,
                    resultKey = KEY_LEARNING_UNIT
                )
            )
        )
    }

    fun onClickRemoveLesson(sectionIndex: Int, linkIndex: Int) {
        val currentMapping = _uiState.value.mapping ?: return
        val currentSections = currentMapping.sections.toMutableList()
        val section = currentSections.getOrNull(sectionIndex) ?: return
        val updatedItems = section.items.toMutableList()
        if (linkIndex !in updatedItems.indices) return
        updatedItems.removeAt(linkIndex)
        currentSections[sectionIndex] = section
            .copy(items = updatedItems)
        updateMapping(currentMapping
            .copy(sections = currentSections))
    }

    fun onLessonTitleChanged(sectionIndex: Int, linkIndex: Int, title: String) {
        val currentMapping = _uiState.value.mapping ?: return
        val currentSections = currentMapping.sections.toMutableList()
        val section = currentSections.getOrNull(sectionIndex) ?: return
        val updatedItems = section.items.toMutableList()
        val link = updatedItems.getOrNull(linkIndex) ?: return
        updatedItems[linkIndex] = link.copy(title = title)
        currentSections[sectionIndex] = section.copy(items = updatedItems)
        updateMapping(currentMapping.copy(sections = currentSections))
    }

    fun onClickSave() {
        val mapping = _uiState.value.mapping ?: return
        if (mapping.title.isBlank()) {
            _uiState.update { it.copy(titleError = Res.string.required.asUiText()) }
            return
        }
        resultReturner.sendResult(
            NavResult(
                key = KEY_SAVED_MAPPING,
                result = mapping
            )
        )
        _navCommandFlow.tryEmit(NavCommand.PopUp())
    }

    fun onClearError() {
        _uiState.update { it.copy(titleError = null) }
    }

    companion object {
        private const val KEY_MAPPING = "curriculum_mapping"
        const val KEY_SAVED_MAPPING = "saved_curriculum_mapping"
    }
}