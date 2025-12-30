package world.respect.shared.viewmodel.assignment.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.DataLoadingState
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.RespectAppDataSource
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.ext.dataOrNull
import world.respect.datalayer.ext.isReadyAndSettled
import world.respect.datalayer.school.ClassDataSource
import world.respect.datalayer.school.model.Assignment
import world.respect.datalayer.school.model.AssignmentAssigneeRef
import world.respect.datalayer.school.model.AssignmentLearningUnitRef
import world.respect.datalayer.school.model.Clazz
import world.respect.lib.opds.model.LangMap
import world.respect.lib.opds.model.OpdsFeedMetadata
import world.respect.lib.opds.model.OpdsGroup
import world.respect.lib.opds.model.OpdsPublication
import world.respect.lib.opds.model.ReadiumLink
import world.respect.lib.opds.model.ReadiumMetadata
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.school.SchoolPrimaryKeyGenerator
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.add_assignment
import world.respect.shared.generated.resources.edit_assignment
import world.respect.shared.generated.resources.required_field
import world.respect.shared.generated.resources.save
import world.respect.shared.navigation.AssignmentDetail
import world.respect.shared.navigation.AssignmentEdit
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.NavResultReturner
import world.respect.shared.navigation.RespectAppLauncher
import world.respect.shared.navigation.RouteResultDest
import world.respect.shared.resources.UiText
import world.respect.shared.util.LaunchDebouncer
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.ActionBarButtonUiState
import world.respect.shared.viewmodel.curriculum.mapping.model.CurriculumMapping
import world.respect.shared.viewmodel.learningunit.LearningUnitSelection
import kotlin.time.Clock

data class AssignmentEditUiState(
    val assignment: DataLoadState<Assignment> = DataLoadingState(),
    val assigneeText: String = "",
    val nameError: UiText? = null,
    val classOptions: List<Clazz> = emptyList(),
    val classError: UiText? = null,
    val learningUnitInfoFlow: (Url) -> Flow<DataLoadState<OpdsPublication>> = { flowOf(DataLoadingState()) },
    val showPlaylistButton: Boolean = true,
) {
    val fieldsEnabled: Boolean
        get() = assignment.isReadyAndSettled()

    val hasErrors: Boolean
        get() = nameError != null || classError != null
}

class AssignmentEditViewModel(
    savedStateHandle: SavedStateHandle,
    accountManager: RespectAccountManager,
    private val json: Json,
    private val resultReturner: NavResultReturner,
    private val respectAppDataSource: RespectAppDataSource,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()

    private val route: AssignmentEdit = savedStateHandle.toRoute()

    private val schoolDataSource: SchoolDataSource by inject()

    private val _uiState = MutableStateFlow(AssignmentEditUiState())

    val uiState = _uiState.asStateFlow()

    private val debouncer = LaunchDebouncer(viewModelScope)

    private val schoolPrimaryKeyGenerator: SchoolPrimaryKeyGenerator by inject()

    private val uid = route.guid ?: schoolPrimaryKeyGenerator.primaryKeyGenerator.nextId(
        Assignment.TABLE_ID
    ).toString()

    private fun LearningUnitSelection.toRef(): AssignmentLearningUnitRef {
        return AssignmentLearningUnitRef(
            learningUnitManifestUrl = this.learningUnitManifestUrl,
            appManifestUrl = this.appManifestUrl,
        )
    }

    init {
        _appUiState.update { prev ->
            prev.copy(
                title = if(route.guid == null) {
                    Res.string.add_assignment.asUiText()
                }else {
                    Res.string.edit_assignment.asUiText()
                },
                userAccountIconVisible = false,
                actionBarButtonState = ActionBarButtonUiState(
                    visible = true,
                    text = Res.string.save.asUiText(),
                    onClick = ::onClickSave,
                ),
                hideBottomNavigation = true,
            )
        }

        viewModelScope.launch {
            resultReturner.filteredResultFlowForKey(KEY_LEARNING_UNIT).collect { result ->
                val learningUnit = result.result as? LearningUnitSelection ?: return@collect
                val assignmentResourceRef = learningUnit.toRef()

                _uiState.update { prev ->
                    val prevAssignment = prev.assignment.dataOrNull() ?: return@update prev

                    prev.copy(
                        assignment = DataReadyState(
                            data = prevAssignment.copy(
                                learningUnits = prevAssignment.learningUnits + assignmentResourceRef
                            )
                        )
                    )
                }
            }
        }
        viewModelScope.launch {
            resultReturner.filteredResultFlowForKey(KEY_PLAYLIST_SELECTION).collect { result ->
                val mapping = result.result as? CurriculumMapping ?: return@collect
                val group = convertMappingToOpdsGroup(mapping)

                viewModelScope.launch {
                    val assignment = _uiState.value.assignment.dataOrNull() ?: return@launch
                    val newLearningUnits = loadLessonsFromOpdsGroup(group)

                    val existingUrls = assignment.learningUnits.map {
                        it.learningUnitManifestUrl
                    }.toSet()

                    val uniqueNewUnits = newLearningUnits.filter {
                        it.learningUnitManifestUrl !in existingUrls
                    }

                    _uiState.update { prev ->
                        prev.copy(
                            assignment = DataReadyState(
                                assignment.copy(
                                    learningUnits = assignment.learningUnits + uniqueNewUnits
                                )
                            )
                        )
                    }
                }
            }
        }

        launchWithLoadingIndicator {
            val classes = schoolDataSource.classDataSource.list(
                DataLoadParams(),
                ClassDataSource.GetListParams()
            ).dataOrNull() ?: emptyList()

            _uiState.update {
                it.copy(
                    classOptions = classes,
                    learningUnitInfoFlow = ::learningUnitInfoFlowFor
                )
            }

            if(route.guid != null) {
                loadEntity(
                    json = json,
                    serializer = Assignment.serializer(),
                    loadFn = { params ->
                        schoolDataSource.assignmentDataSource.findByGuid(
                            params, route.guid
                        )
                    },
                    uiUpdateFn = { entity ->
                        _uiState.update { prev ->
                            val assigneeClassUid = entity.dataOrNull()?.assignees?.firstOrNull()?.uid
                            prev.copy(
                                assignment = entity,
                                assigneeText = classes.firstOrNull {
                                    it.guid == assigneeClassUid
                                }?.title ?: ""
                            )
                        }
                    }
                )

                viewModelScope.launch {
                    schoolDataSource.assignmentDataSource.findByGuidAsFlow(
                        route.guid
                    ).collect { assignmentState ->
                        if (assignmentState is DataReadyState) {
                            val currentLearningUnits = _uiState.value.assignment.dataOrNull()?.learningUnits
                            val newLearningUnits = assignmentState.data.learningUnits
                            if (currentLearningUnits != newLearningUnits) {
                                _uiState.update { prev ->
                                    prev.copy(assignment = assignmentState)
                                }
                            }
                        }
                    }
                }
            }else {
                val initialLearningUnits = route.learningUnitSelectedList?.map { it.toRef() } ?: emptyList()

                _uiState.update { prev ->
                    prev.copy(
                        assignment = DataReadyState(
                            Assignment(
                                uid = uid,
                                title = "",
                                description = "",
                                learningUnits = initialLearningUnits
                            )
                        )
                    )
                }
            }
        }
    }

    /**
     * Convert CurriculumMapping to OpdsGroup for processing multiple lessons
     */
    private fun convertMappingToOpdsGroup(mapping: CurriculumMapping): OpdsGroup {
        return OpdsGroup(
            metadata = OpdsFeedMetadata(
                title = mapping.title
            ),
            publications = mapping.sections.flatMap { section ->
                section.items.map { link ->
                    OpdsPublication(
                        metadata = ReadiumMetadata(
                            title = mapOf("en" to (link.title ?: "Untitled")) as LangMap,
                        ),
                        links = listOfNotNull(
                            ReadiumLink(
                                href = link.href,
                                rel = listOf("http://opds-spec.org/acquisition"),
                            ),
                            link.appManifestUrl?.let {
                                ReadiumLink(
                                    href = it.toString(),
                                    rel = listOf("http://opds-spec.org/compatible-app"),
                                )
                            }
                        )
                    )
                }
            }
        )
    }

    fun onClickAddFromCurriculum() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                RespectAppLauncher.create(
                    resultDest = RouteResultDest(
                        resultPopUpTo = route,
                        resultKey = KEY_PLAYLIST_SELECTION,
                    )
                )
            )
        )
    }

    private suspend fun loadLessonsFromOpdsGroup(group: OpdsGroup): List<AssignmentLearningUnitRef> {
        val publications = group.publications ?: emptyList()

        return publications.mapNotNull { publication ->
            try {
                val acquisitionLink = publication.links.firstOrNull { link ->
                    link.rel?.any { it.startsWith("http://opds-spec.org/acquisition") } == true
                } ?: return@mapNotNull null

                val publicationUrl = Url(acquisitionLink.href)

                val appManifestLink = publication.links.firstOrNull { link ->
                    link.rel?.contains("http://opds-spec.org/compatible-app") == true
                }

                val appManifestUrl = appManifestLink?.let { Url(it.href) }
                    ?: return@mapNotNull null

                AssignmentLearningUnitRef(
                    learningUnitManifestUrl = publicationUrl,
                    appManifestUrl = appManifestUrl
                )
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    fun learningUnitInfoFlowFor(url: Url): Flow<DataLoadState<OpdsPublication>> {
        return respectAppDataSource.opdsDataSource.loadOpdsPublication(
            url = url, params = DataLoadParams(), null, null
        )
    }

    fun onAssigneeClassSelected(clazz: Clazz) {
        val assignment = _uiState.value.assignment.dataOrNull() ?: return
        _uiState.update {
            it.copy(
                assignment = DataReadyState(
                    assignment.copy(
                        assignees = listOf(
                            AssignmentAssigneeRef(uid = clazz.guid)
                        )
                    )
                ),
                assigneeText = clazz.title,
                classError = null,
            )
        }
    }

    fun onEntityChanged(assignment: Assignment) {
        _uiState.update { prev ->
            prev.copy(
                assignment = DataReadyState(assignment),
                nameError = prev.nameError?.takeIf {
                    prev.assignment.dataOrNull()?.title == assignment.title
                },
            )
        }

        debouncer.launch(DEFAULT_SAVED_STATE_KEY) {
            savedStateHandle[DEFAULT_SAVED_STATE_KEY] = json.encodeToString(
                Assignment.serializer(),
                assignment
            )
        }
    }

    fun onAssigneeTextChanged(text: String) {
        _uiState.update {
            it.copy(assigneeText = text, classError = null)
        }
    }

    fun onClickAddLearningUnit() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                RespectAppLauncher.create(
                    resultDest = RouteResultDest(
                        resultPopUpTo = route,
                        resultKey = KEY_LEARNING_UNIT,
                    )
                )
            )
        )
    }

    fun onClickRemoveLearningUnit(ref: AssignmentLearningUnitRef) {
        val assignment = uiState.value.assignment.dataOrNull() ?: return

        _uiState.update { prev ->
            prev.copy(
                assignment = DataReadyState(
                    data = assignment.copy(
                        learningUnits = assignment.learningUnits.filter {
                            it.learningUnitManifestUrl != ref.learningUnitManifestUrl
                        }
                    )
                )
            )
        }
    }

    fun onClickSave() {
        val stateToSave = _uiState.updateAndGet { prev ->
            val assignmentVal = prev.assignment.dataOrNull()

            prev.copy(
                nameError = Res.string.required_field.asUiText().takeIf {
                    assignmentVal?.title.isNullOrBlank()
                },
                classError = Res.string.required_field.asUiText().takeIf {
                    assignmentVal?.assignees?.isEmpty() != false
                }
            )
        }

        if(stateToSave.hasErrors)
            return

        val assignment = uiState.value.assignment.dataOrNull() ?: return

        launchWithLoadingIndicator {
            schoolDataSource.assignmentDataSource.store(
                listOf(assignment.copy(lastModified = Clock.System.now()))
            )

            if(route.guid == null) {
                _navCommandFlow.tryEmit(
                    NavCommand.Navigate(
                        destination = AssignmentDetail(uid = uid),
                        popUpTo = route,
                        popUpToInclusive = true,
                    )
                )
            }else {
                _navCommandFlow.tryEmit(NavCommand.PopUp())
            }
        }
    }

    companion object {
        const val KEY_LEARNING_UNIT = "result_learning_unit"
        const val KEY_PLAYLIST_SELECTION = "result_playlist_selection"
    }
}