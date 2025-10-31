package world.respect.shared.viewmodel.curriculum.mapping.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import world.respect.shared.domain.curriculum.mapping.GetCurriculumMappingsUseCase
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.error_unknown
import world.respect.shared.generated.resources.mapping_edit
import world.respect.shared.generated.resources.required
import world.respect.shared.generated.resources.save
import world.respect.shared.generated.resources.save_failed
import world.respect.shared.navigation.CurriculumMappingEdit
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.NavResultReturner
import world.respect.shared.navigation.RespectAppLauncher
import world.respect.shared.resources.UiText
import world.respect.shared.util.LaunchDebouncer
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
    val pendingLessonSectionIndex: Int? = null ,
) {

    val fieldsEnabled: Boolean
        get() = !loading

    val title: String
        get() = mapping?.title ?: ""

    val description: String
        get() = mapping?.description ?: ""

    val sections: List<CurriculumMappingSection>
        get() = mapping?.sections ?:emptyList()

}

class CurriculumMappingEditViewModel(
    savedStateHandle: SavedStateHandle,
    private val getCurriculumMappingsUseCase: GetCurriculumMappingsUseCase,
    private val resultReturner: NavResultReturner,
) : RespectViewModel(savedStateHandle) {

    private val route: CurriculumMappingEdit = savedStateHandle.toRoute()
    private val mappingUid  = route.textbookUid

    private val _uiState = MutableStateFlow(
        CurriculumMappingEditUiState(isNew = mappingUid == 0L)
    )
    val uiState = _uiState.asStateFlow()

    private val debouncer = LaunchDebouncer(viewModelScope)

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
                hideBottomNavigation = true,
            )
        }

        loadMappingData()

        viewModelScope.launch {
            resultReturner.filteredResultFlowForKey(KEY_LEARNING_UNIT).collect { result ->
                val learningUnit = result.result as? LearningUnitSelection ?: return@collect
                val pendingSectionIndex = _uiState.value.pendingLessonSectionIndex ?: return@collect

                val currentMapping = _uiState.value.mapping ?: return@collect
                val currentSections = currentMapping.sections.toMutableList()

                if (pendingSectionIndex >= currentSections.size) return@collect

                val section = currentSections[pendingSectionIndex]
                val newLink = CurriculumMappingSectionLink(
                    href = learningUnit.learningUnitManifestUrl,
                    title = ""
                )

                currentSections[pendingSectionIndex] = CurriculumMappingSection(
                    title = section.title,
                    items = section.items + newLink
                )

                _uiState.update { prev ->
                    prev.copy(
                        mapping = currentMapping.copy(sections = currentSections),
                        pendingLessonSectionIndex = null
                    )
                }
            }
        }
    }

    private fun loadMappingData() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }

            try {
                if (mappingUid != 0L) {
                    val loadedMapping = getCurriculumMappingsUseCase.getMappingByUid(mappingUid)
                    if (loadedMapping != null) {
                        _uiState.update { prev ->
                            prev.copy(
                                mapping = loadedMapping,
                                loading = false,
                                isNew = false,
                                error = null
                            )
                        }
                    } else {
                        _uiState.update { prev ->
                            prev.copy(
                                loading = false,
                                error = Res.string.error_unknown.asUiText())
                        }
                    }
                } else {
                    val newMapping = CurriculumMapping(
                        title = "",
                        sections = emptyList()
                    )
                    _uiState.update { prev ->
                        prev.copy(
                            mapping = newMapping,
                            loading = false,
                            error = null
                        )
                    }
                }
            } catch (e: Throwable) {
                _uiState.update { prev ->
                    prev.copy(
                        loading = false,
                        error = Res.string.error_unknown.asUiText()
                    )
                }
            }
        }
    }

    fun onTitleChanged(title: String) {
        val currentMapping = _uiState.value.mapping ?: return
        val updatedMapping = currentMapping.copy(title = title)

        _uiState.update { prev ->
            prev.copy(mapping = updatedMapping,
                titleError = null
            )
        }

        debouncer.launch(DEFAULT_SAVED_STATE_KEY) {
            savedStateHandle[DEFAULT_SAVED_STATE_KEY] = updatedMapping.title
        }
    }
    fun onDescriptionChanged(description: String) {
        val currentMapping = _uiState.value.mapping ?: return
        val updatedMapping = currentMapping.copy(description = description)

        _uiState.update { prev ->
            prev.copy(mapping = updatedMapping)
        }
    }

    fun onClickAddSection() {
        val currentMapping = _uiState.value.mapping ?: return
        val newSection = CurriculumMappingSection(
            title = "",
            items = emptyList()
        )
        val updatedSections = currentMapping.sections + newSection

        _uiState.update { prev ->
            prev.copy(
                mapping = currentMapping.copy(
                    sections = updatedSections
                )
            )
        }
    }
    fun onSectionTitleChanged(sectionIndex: Int, title: String) {
        val currentMapping = _uiState.value.mapping ?: return
        val currentSections = currentMapping.sections.toMutableList()

        if (sectionIndex >= currentSections.size) return

        val section = currentSections[sectionIndex]
        currentSections[sectionIndex] = CurriculumMappingSection(
            title = title,
            items = section.items
        )

        _uiState.update { prev ->
            prev.copy(
                mapping = currentMapping.copy(sections = currentSections)
            )
        }
    }

    fun onClickAddBookCover() {
        // TODO:
    }

    fun onClickRemoveSection(sectionIndex: Int)  {
        val currentMapping = _uiState.value.mapping ?: return
        val currentSections = currentMapping.sections.toMutableList()
        if (sectionIndex >= currentSections.size) return

        currentSections.removeAt(sectionIndex)


        _uiState.update { prev ->
            prev.copy(
                mapping = currentMapping.copy(sections = currentSections)
            )
        }
    }

    fun onSectionMoved(fromIndex: Int, toIndex: Int)  {
        val currentMapping = _uiState.value.mapping ?: return
        val currentSections = currentMapping.sections.toMutableList()
        if (fromIndex >= currentSections.size || toIndex >= currentSections.size) return

        val section = currentSections.removeAt(fromIndex)
        currentSections.add(toIndex, section)

        _uiState.update { prev ->
            prev.copy(
            mapping = currentMapping.copy(sections = currentSections)
            )
        }
    }



    fun onClickAddLesson(sectionIndex: Int) {
        _uiState.update { prev ->
            prev.copy(pendingLessonSectionIndex = sectionIndex)
        }

        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                RespectAppLauncher.create(
                    resultPopUpTo = CurriculumMappingEdit::class,
                    resultKey = KEY_LEARNING_UNIT,
                )
            )
        )
    }



    fun onClickRemoveLesson(sectionIndex: Int, linkIndex: Int) {
        val currentMapping = _uiState.value.mapping ?: return
        val currentSections = currentMapping.sections.toMutableList()

        if (sectionIndex >= currentSections.size) return

        val section = currentSections[sectionIndex]
        val updatedItems = section.items.toMutableList()

        if (linkIndex >= updatedItems.size) return

        updatedItems.removeAt(linkIndex)

        currentSections[sectionIndex] = CurriculumMappingSection(
            title = section.title,
            items = updatedItems
        )

        _uiState.update { prev ->
            prev.copy(
                mapping = currentMapping.copy(sections = currentSections)
            )
        }
    }
    fun onLessonTitleChanged(sectionIndex: Int, linkIndex: Int, title: String) {
        val currentMapping = _uiState.value.mapping ?: return
        val currentSections = currentMapping.sections.toMutableList()

        if (sectionIndex >= currentSections.size) return

        val section = currentSections[sectionIndex]
        val updatedItems = section.items.toMutableList()

        if (linkIndex >= updatedItems.size) return

        val link = updatedItems[linkIndex]
        updatedItems[linkIndex] = CurriculumMappingSectionLink(
            href = link.href,
            title = title
        )

        currentSections[sectionIndex] = CurriculumMappingSection(
            title = section.title,
            items = updatedItems
        )

        _uiState.update { prev ->
            prev.copy(
                mapping = currentMapping.copy(sections = currentSections)
            )
        }
    }



    fun onClickSave() {
        val mapping = _uiState.value.mapping ?: return


        mapping.title?.let {
            if (it.isBlank()) {
                _uiState.update { prev ->
                    prev.copy(titleError = Res.string.required.asUiText())
                }
                return
            } else {
                _uiState.update { prev ->
                    prev.copy(titleError = null)
                }
            }
        }

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(loading = true) }

                getCurriculumMappingsUseCase.saveCurriculumMapping(mapping)

                _uiState.update { it.copy(loading = false) }

                _navCommandFlow.tryEmit(NavCommand.PopUp())

            } catch (e: Throwable) {
                _uiState.update {
                    it.copy(
                        loading = false,
                        error = Res.string.save_failed.asUiText()
                    )
                }
            }
        }
    }

    fun onClearError() {
        _uiState.update { prev ->
            prev.copy(
                titleError = null,
                error = null
            )
        }
    }

    fun onRetry() {
        loadMappingData()
    }

}