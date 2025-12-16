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
import world.respect.shared.generated.resources.edit_mapping
import world.respect.shared.generated.resources.edit_playlist
import world.respect.shared.generated.resources.required_field
import world.respect.shared.generated.resources.save
import world.respect.shared.navigation.CurriculumMappingEdit
import world.respect.shared.navigation.LearningUnitDetail
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
    private val mappingData = route.mappingData

    private val _uiState = MutableStateFlow(
        CurriculumMappingEditUiState(
            mapping = mappingData ?: CurriculumMapping(uid = mappingUid),
            isNew = mappingUid == 0L
        )
    )

    val uiState = _uiState.asStateFlow()

    init {
        _appUiState.update { prev ->
            prev.copy(
                title = Res.string.edit_playlist.asUiText(),
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
                                        title = selectedLearningUnit.selectedPublication.metadata.title.getTitle(),
                                        appManifestUrl = selectedLearningUnit.appManifestUrl
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

    fun onLessonMovedBetweenSections(
        fromSectionIndex: Int,
        fromLinkIndex: Int,
        toSectionIndex: Int,
        toLinkIndex: Int
    ) {
        updateUiStateAndCommit { prev ->
            val mapping = prev.mapping
            if (mapping == null) {
                prev
            } else if (fromSectionIndex == toSectionIndex) {
                prev.copy(
                    mapping = mapping.copy(
                        sections = mapping.sections.updateAtIndex(fromSectionIndex) { section ->
                            section.copy(
                                items = section.items.moveItem(from = fromLinkIndex, to = toLinkIndex)
                            )
                        }
                    )
                )
            } else {
                val fromSection = mapping.sections[fromSectionIndex]
                val lessonToMove = fromSection.items[fromLinkIndex]

                val updatedFromSection = fromSection.copy(
                    items = fromSection.items.filterIndexed { index, _ -> index != fromLinkIndex }
                )

                val toSection = mapping.sections[toSectionIndex]
                val updatedToSection = toSection.copy(
                    items = buildList {
                        addAll(toSection.items.take(toLinkIndex))
                        add(lessonToMove)
                        addAll(toSection.items.drop(toLinkIndex))
                    }
                )

                prev.copy(
                    mapping = mapping.copy(
                        sections = mapping.sections.mapIndexed { index, section ->
                            when (index) {
                                fromSectionIndex -> updatedFromSection
                                toSectionIndex -> updatedToSection
                                else -> section
                            }
                        }
                    )
                )
            }
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
        updateUiStateAndCommit { prev ->
            prev.copy(
                mapping = prev.mapping?.copy(
                    sections = prev.mapping.sections.updateAtIndex(sectionIndex) { section ->
                        section.copy(
                            items = section.items.filterIndexed { index, _ ->  index != linkIndex }
                        )
                    }
                )
            )
        }
    }

    fun onClickLesson(link: CurriculumMappingSectionLink) {
        val publicationUrl = Url(link.href)
        val appManifestUrl = link.appManifestUrl ?: return

        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                LearningUnitDetail.create(
                    learningUnitManifestUrl = publicationUrl,
                    appManifestUrl = appManifestUrl,
                    refererUrl = publicationUrl,
                    expectedIdentifier = null
                )
            )
        )
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