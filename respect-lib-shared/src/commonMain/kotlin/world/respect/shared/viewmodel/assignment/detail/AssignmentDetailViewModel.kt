package world.respect.shared.viewmodel.assignment.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.db.school.ext.fullName
import world.respect.datalayer.db.school.ext.isAdminOrTeacher
import world.respect.datalayer.db.school.ext.isStudent
import world.respect.datalayer.school.model.AssignmentLearningUnitRef
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.DataLoadingState
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.lib.dataloadstate.ext.firstOrNotLoaded
import world.respect.lib.dataloadstate.ext.map
import world.respect.lib.opds.model.OpdsPublication
import world.respect.lib.xapi.composites.XapiActorAndAssignmentProgress
import world.respect.lib.xapi.composites.XapiAssignmentProgress
import world.respect.lib.xapi.model.VERB_ASSIGN
import world.respect.lib.xapi.model.XapiStatement
import world.respect.lib.xapi.ext.calculatePercentage
import world.respect.lib.xapi.ext.isCompleted
import world.respect.lib.xapi.ext.isStarted
import world.respect.lib.xapi.ext.personName
import world.respect.lib.xapi.ext.personUid
import world.respect.lib.xapi.resources.XapiStatementsResource.GetStatementParams
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.xapi.activityDefinitionTitle
import world.respect.shared.domain.xapi.assignmentLearningUnits
import world.respect.shared.domain.xapi.isAssignmentStatement
import world.respect.shared.ext.whenSubscribed
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.edit
import world.respect.shared.navigation.AssignmentDetail
import world.respect.shared.navigation.AssignmentEdit
import world.respect.shared.navigation.LearningUnitDetail
import world.respect.shared.navigation.NavCommand
import world.respect.shared.util.AssignmentStatusFilter
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.FabUiState
import kotlin.uuid.ExperimentalUuidApi


data class AssignmentDetailUiState(
    val xApiStatement: DataLoadState<XapiStatement> = DataLoadingState(),
    val taskInfoFlow: (Url) -> Flow<DataLoadState<OpdsPublication>> = {
        flowOf(DataLoadingState())
    },
    val assignmentProgressList: List<XapiActorAndAssignmentProgress> = emptyList(),
    val selectedStatusFilter: AssignmentStatusFilter = AssignmentStatusFilter.ALL,
    val isFullscreen: Boolean = false,
    val isStudent: Boolean = false,
    val assigneeStudentName: String = "",
    val personGuid: String = "",
    val canEdit: Boolean = false,
) {

    /**
     * All tasks associated with this assignment, extracted from the assignment's xAPI definition.
     */
    val tasks: List<AssignmentLearningUnitRef>
        get() = xApiStatement.dataOrNull()?.assignmentLearningUnits ?: emptyList()

    private val taskActivityIds: List<String>
        get() = tasks.map { it.learningUnitManifestUrl.toString() }


    val numStudents: Int get() = assignmentProgressList.size

    val numCompleted: Int get() = assignmentProgressList.count {
        it.isCompleted(taskActivityIds)
    }

    val numInProgress: Int get() = assignmentProgressList.count {
        it.isStarted && !it.isCompleted(taskActivityIds)
    }

    val numNotStarted: Int get() = assignmentProgressList.count { !it.isStarted }


    val rowsToDisplay: List<XapiActorAndAssignmentProgress>
        get() = when (selectedStatusFilter) {
            AssignmentStatusFilter.ALL -> assignmentProgressList
            AssignmentStatusFilter.COMPLETED -> assignmentProgressList.filter {
                it.isCompleted(taskActivityIds)
            }
            AssignmentStatusFilter.IN_PROGRESS -> assignmentProgressList.filter {
                it.isStarted && !it.isCompleted(taskActivityIds)
            }
            AssignmentStatusFilter.NOT_STARTED -> assignmentProgressList.filter {
                !it.isStarted
            }
        }

    val statusCounts: Map<AssignmentStatusFilter, Int>
        get() = mapOf(
            AssignmentStatusFilter.ALL to numStudents,
            AssignmentStatusFilter.COMPLETED to numCompleted,
            AssignmentStatusFilter.IN_PROGRESS to numInProgress,
            AssignmentStatusFilter.NOT_STARTED to numNotStarted
        )

    /**
     * Progress map for ALL students (not just filtered) - used for averages
     */
    val allProgressMap: Map<String, Map<String, XapiAssignmentProgress>>
        get() = assignmentProgressList.associate { row ->
            row.personUid to row.progress.associateBy { it.activityId }
        }

    /**
     * Calculates the average completion percentage across all tasks for a specific student.
     * Returns null if student has no progress entries for any task.
     */
    fun getAverageForStudent(personUid: String): Double? {
        val studentProgress = allProgressMap[personUid] ?: return null

        val percentages = tasks.mapNotNull { task ->
            studentProgress[task.learningUnitManifestUrl.toString()]?.calculatePercentage()
        }

        return if (percentages.isNotEmpty()) percentages.average() else null
    }
}

@OptIn(ExperimentalUuidApi::class)
class AssignmentDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val accountManager: RespectAccountManager,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()

    private val route: AssignmentDetail = savedStateHandle.toRoute()

    private val schoolDataSource: SchoolDataSource by inject()

    private val _uiState = MutableStateFlow(AssignmentDetailUiState())

    val uiState = _uiState.asStateFlow()

    private val assignmentActivityId: String = route.assignmentActivityId

    init {
        _appUiState.update {
            it.copy(
                fabState = FabUiState(
                    text = Res.string.edit.asUiText(),
                    icon = FabUiState.FabIcon.EDIT,
                    onClick = ::onClickEdit
                )
            )
        }
        _uiState.update {
            it.copy(
                taskInfoFlow = ::taskInfoFlowFor
            )
        }

        // Load the assignment from xAPI statements
        val statementsStream = schoolDataSource.xapiStatementsResource.getAsFlow(
            listParams = GetStatementParams(
                activity = assignmentActivityId,
                verb = VERB_ASSIGN
            ),
            dataLoadParams = DataLoadParams()
        ).map { state ->
            state.map { result ->
                result.statements.filter { it.isAssignmentStatement }
            }.firstOrNotLoaded()
        }.shareIn(viewModelScope, SharingStarted.Lazily)

        viewModelScope.launch {
            statementsStream.collect { statementState ->
                _uiState.update {
                    it.copy(xApiStatement = statementState)
                }
                _appUiState.update {
                    it.copy(title = statementState.dataOrNull()?.activityDefinitionTitle?.asUiText())
                }
                // Only load progress after we have the assignment metadata
                statementState.dataOrNull()?.let {
                    loadAssignmentProgress(assignmentActivityId)
                }
            }
        }

        viewModelScope.launch {
            _uiState.whenSubscribed {
                accountManager.selectedAccountAndPersonFlow.collect { selectedAccount ->
                    val person = selectedAccount?.person
                    val isStudent = person?.isStudent() == true
                    val canEdit = person?.isAdminOrTeacher() == true

                    _uiState.update {
                        it.copy(
                            isStudent = isStudent,
                            assigneeStudentName = person?.fullName() ?: "",
                            personGuid = person?.guid ?: ""
                        )
                    }
                    val isFullscreen = _uiState.value.isFullscreen
                    _appUiState.update {
                        it.copy(
                            hideAppBar = isFullscreen,
                            hideBottomNavigation = isFullscreen,
                            fabState = it.fabState.copy(
                                visible = canEdit && !isFullscreen
                            ),
                            fullscreenToggleVisible = true,
                            isFullscreen = isFullscreen,
                            onToggleFullscreen = ::onToggleFullscreen
                        )
                    }
                }
            }
        }
    }

    private fun loadAssignmentProgress(activityId: String) {
        viewModelScope.launch {
            schoolDataSource.xapiStatementsResource
                .getAssignmentProgress(activityId = activityId)
                .collect { progressState ->
                    val progressList = progressState.dataOrNull() ?: emptyList()
                    _uiState.update {
                        it.copy(assignmentProgressList = progressList)
                    }
                }
        }
    }

    fun onClickEdit() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                AssignmentEdit.create(
                    assignmentActivityId = route.assignmentActivityId
                )
            )
        )
    }

    fun taskInfoFlowFor(url: Url): Flow<DataLoadState<OpdsPublication>> {
        return schoolDataSource.opdsPublicationDataSource.getByUrlAsFlow(
            url = url, params = DataLoadParams(), null, null
        )
    }

    fun onClickTask(ref: AssignmentLearningUnitRef) {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                LearningUnitDetail.create(
                    learningUnitManifestUrl = ref.learningUnitManifestUrl,
                )
            )
        )
    }

    fun onStatusFilterChanged(filter: AssignmentStatusFilter) {
        _uiState.update {
            it.copy(selectedStatusFilter = filter)
        }
    }

    fun onToggleFullscreen() {
        _uiState.update { it.copy(isFullscreen = !it.isFullscreen) }
        val currentState = _uiState.value
        _appUiState.update {
            it.copy(
                hideAppBar = currentState.isFullscreen,
                hideBottomNavigation = currentState.isFullscreen,
                fabState = it.fabState.copy(
                    visible = currentState.canEdit && !currentState.isFullscreen
                ),
                fullscreenToggleVisible = true,
                isFullscreen = currentState.isFullscreen,
                onToggleFullscreen = ::onToggleFullscreen
            )
        }
    }
}
