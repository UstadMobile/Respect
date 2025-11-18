package world.respect.shared.viewmodel.curriculum.mapping.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.RespectAppDataSource
import world.respect.datalayer.ext.map
import world.respect.lib.opds.model.findIcons
import world.respect.libutil.ext.moveItem
import world.respect.libutil.ext.updateAtIndex
import world.respect.libutil.ext.resolve
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.error_no_current_mapping
import world.respect.shared.generated.resources.edit_mapping
import world.respect.shared.generated.resources.required_field
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
import world.respect.shared.navigation.RouteResultDest
import world.respect.shared.viewmodel.app.appstate.getTitle

data class CurriculumMappingEditUiState(
    val mapping: CurriculumMapping? = null,
    val loading: Boolean = false,
    val isNew: Boolean = true,
    val titleError: UiText? = null,
    val error: UiText? = null,
    val pendingLessonSectionIndex: Int? = null,
    val sectionUiState: (CurriculumMappingSection) -> Flow<CurriculumMappingSectionUiState> = { emptyFlow() },
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

data class CurriculumMappingSectionUiState(
    val icon: Url? = null,
)

class CurriculumMappingEditViewModel(
    savedStateHandle: SavedStateHandle,
    private val resultReturner: NavResultReturner,
    private val json: Json,
    private val respectAppDataSource: RespectAppDataSource,
) : RespectViewModel(savedStateHandle) {

    private val route: CurriculumMappingEdit = savedStateHandle.toRoute()

    private val mappingUid = route.textbookUid

    private val _uiState = MutableStateFlow(
        CurriculumMappingEditUiState(
            mapping = CurriculumMapping(uid = mappingUid),
            isNew = mappingUid == 0L
        )
    )

    val uiState = _uiState.asStateFlow()

    init {
        _appUiState.update { prev ->
            prev.copy(
                title = Res.string.edit_mapping.asUiText(),
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
            resultReturner.filteredResultFlowForKey(
                KEY_LEARNING_UNIT
            ).collect { result ->
                val selectedLearningUnit = result.result as? LearningUnitSelection ?: return@collect
                val pendingSectionIndex = _uiState.value.pendingLessonSectionIndex ?: return@collect

                updateUiStateAndCommit { prev ->
                    prev.copy(
                        mapping = prev.mapping?.copy(
                            sections = prev.mapping.sections.updateAtIndex(pendingSectionIndex) {
                                it.copy(
                                    items = it.items + CurriculumMappingSectionLink(
                                        href = selectedLearningUnit.learningUnitManifestUrl.toString(),
                                        title = selectedLearningUnit.selectedPublication.metadata.title.getTitle()
                                    )
                                )
                            }
                        ),
                        pendingLessonSectionIndex = null,
                    )
                }
            }
        }
    }

    private fun updateUiStateAndCommit(block: (CurriculumMappingEditUiState) -> CurriculumMappingEditUiState) {
        val mappingToCommit = _uiState.updateAndGet(block).mapping ?: return

        savedStateHandle[KEY_MAPPING] = json.encodeToString(
            CurriculumMapping.serializer(), mappingToCommit
        )
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
        updateUiStateAndCommit { prev ->
            prev.copy(
                mapping = prev.mapping?.copy(title = title),
                titleError = null,
            )
        }
    }

    fun onDescriptionChanged(description: String) {
        updateUiStateAndCommit { prev ->
            prev.copy(
                mapping = prev.mapping?.copy(description = description)
            )
        }
    }

    fun onClickAddSection() {
        updateUiStateAndCommit { prev ->
            prev.copy(
                mapping = prev.mapping?.copy(
                    sections = prev.mapping.sections + CurriculumMappingSection(title = "")
                )
            )
        }
    }

    fun onSectionTitleChanged(sectionIndex: Int, title: String) {
        updateUiStateAndCommit { prev ->
            prev.copy(
                mapping = prev.mapping?.copy(
                    sections = prev.mapping.sections.updateAtIndex(sectionIndex) {
                        it.copy(title = title)
                    }
                )
            )
        }
    }

    fun onClickRemoveSection(sectionIndex: Int) {
        updateUiStateAndCommit { prev ->
            prev.copy(
                mapping = prev.mapping?.copy(
                    sections = prev.mapping.sections.filterIndexed { index, _ ->
                        index != sectionIndex
                    }
                )
            )
        }
    }

    fun onSectionMoved(fromIndex: Int, toIndex: Int) {
        updateUiStateAndCommit { prev ->
            prev.copy(
                mapping = prev.mapping?.copy(
                    sections = prev.sections.moveItem(from = fromIndex, to = toIndex)
                )
            )
        }
    }

    fun onClickAddLesson(sectionIndex: Int) {
        _uiState.update { it.copy(pendingLessonSectionIndex = sectionIndex) }
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                RespectAppLauncher.create(
                    resultDest = RouteResultDest(
                        resultPopUpTo = route,
                        resultKey = KEY_LEARNING_UNIT
                    )
                )
            )
        )
    }

    fun onClickRemoveLesson(sectionIndex: Int, linkIndex: Int) {
        val currentMapping = _uiState.value.mapping
        if (currentMapping == null) {
            _uiState.update { it.copy(error = Res.string.error_no_current_mapping.asUiText()) }
            return
        }
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
        val currentMapping = _uiState.value.mapping
        if (currentMapping == null) {
            _uiState.update { it.copy(error = Res.string.error_no_current_mapping.asUiText()) }
            return
        }
        val currentSections = currentMapping.sections.toMutableList()
        val section = currentSections.getOrNull(sectionIndex) ?: return
        val updatedItems = section.items.toMutableList()
        val link = updatedItems.getOrNull(linkIndex) ?: return
        updatedItems[linkIndex] = link.copy(title = title)
        currentSections[sectionIndex] = section.copy(items = updatedItems)
        updateMapping(currentMapping.copy(sections = currentSections))
    }

    /**
     * Provide a flow that creates the SectionLinkUiState .
     */
    fun sectionLinkUiStateFor(
        link: CurriculumMappingSectionLink
    ): Flow<DataLoadState<CurriculumMappingSectionUiState>> {
        val publicationUrl = Url(link.href)
        return respectAppDataSource.opdsDataSource.loadOpdsPublication(
            url = Url(link.href),
            params = DataLoadParams(),
            referrerUrl = null,
            expectedPublicationId = null,
        ).map { opdsLoadState ->
            opdsLoadState.map { publication ->
                CurriculumMappingSectionUiState(
                    icon = publication.findIcons().firstOrNull()?.let {
                        publicationUrl.resolve(it.href)
                    }
                )
            }
        }
    }

    fun onClickSave() {
        val mapping = _uiState.value.mapping ?: return
        if (mapping.title.isBlank()) {
            _uiState.update { it.copy(titleError = Res.string.required_field.asUiText()) }
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