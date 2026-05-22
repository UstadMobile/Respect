package world.respect.shared.viewmodel.assignment.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.db.school.ext.isAdminOrTeacher
import world.respect.datalayer.db.school.ext.isStudent
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.DataLoadingState
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.lib.opds.model.OpdsPublication
import world.respect.lib.xapi.composites.AssignmentAndProgress
import world.respect.lib.xapi.composites.XapiActorAndAssignmentProgress
import world.respect.lib.xapi.ext.calculatePercentage
import world.respect.lib.xapi.ext.isCompleted
import world.respect.lib.xapi.ext.isStarted
import world.respect.lib.xapi.ext.personUid
import world.respect.lib.xapi.model.XapiActivity
import world.respect.lib.xapi.model.XapiActor
import world.respect.lib.xapi.model.XapiVerb
import world.respect.lib.xapi.resources.XapiStatementsResource
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.xapi.activityDefinitionTitle
import world.respect.shared.domain.xapi.manifestUrl
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
    val taskInfoFlow: (Url) -> Flow<DataLoadState<OpdsPublication>> = {
        flowOf(DataLoadingState())
    },
    val assignmentProgress: DataLoadState<AssignmentAndProgress> = DataLoadingState(),
    val selectedStatusFilter: AssignmentStatusFilter = AssignmentStatusFilter.ALL,
    val isFullscreen: Boolean = false,
    val isStudent: Boolean = false,
    val canEdit: Boolean = false,
    val filterActor: XapiActor? = null
) {

    /**
     * All tasks associated with this assignment, extracted from the assignment's xAPI definition.
     */
    val tasks: List<XapiActivity>
        get() = assignmentProgress.dataOrNull()?.assignmentStatement?.context?.contextActivities?.grouping ?: emptyList()

    private val taskActivityIds: List<String>
        get() = tasks.map { it.id }


    val assignmentProgressList: List<XapiActorAndAssignmentProgress>
        get() = assignmentProgress.dataOrNull()?.progress ?: emptyList()

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
     * Calculates the average completion percentage across all tasks for a specific student.
     * Returns null if student has no progress entries for any task.
     */
    fun getAverageForStudent(personUid: String): Double? {
        val studentProgress = assignmentProgressList.find { it.personUid == personUid }
            ?: return null

        val percentages = tasks.mapNotNull { task ->
            studentProgress.progress
                .find { it.activityId == task.id }
                ?.calculatePercentage()
        }

        return if (percentages.isNotEmpty()) percentages.average() else null
    }
}
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


    // Create a shared flow for assignment progress

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

        viewModelScope.launch {
            accountManager.selectedAccountAndPersonFlow.filterNotNull().distinctUntilChanged().collectLatest { sessionAndPerson ->
                val isStudent = sessionAndPerson.person.isStudent()
                val canEdit = sessionAndPerson.person.isAdminOrTeacher()

                _uiState.update {
                    it.copy(
                        isStudent = isStudent,
                        canEdit = canEdit
                    )
                }

                val assignmentProgressFlow = schoolDataSource.xapiStatementsResource.getAssignmentProgress(
                    activityId = route.assignmentActivityId,
                    filterByActor = if(sessionAndPerson.person.isStudent()) {
                        sessionAndPerson.xapiAgent
                    }else {
                        null
                    }
                ).shareIn(viewModelScope, SharingStarted.Lazily)

                launch {
                    assignmentProgressFlow.collect { assignmentAndProgress ->
                        _appUiState.update { appState ->
                            appState.copy(
                                title = assignmentAndProgress.dataOrNull()?.assignmentStatement
                                    ?.activityDefinitionTitle?.asUiText()
                            )
                        }

                        _uiState.update {
                            it.copy(assignmentProgress = assignmentAndProgress)
                        }
                    }
                }

                launch {
                    //Load the statement for the assigned actor (e.g. group) to ensure the entire definition is loaded
                    assignmentProgressFlow.mapNotNull {
                        it.dataOrNull()?.assignmentStatement?.actor
                    }.distinctUntilChanged().collect { assignedActor ->
                        schoolDataSource.xapiStatementsResource.get(
                            listParams = XapiStatementsResource.GetStatementParams(
                                agent = assignedActor,
                                verb = XapiVerb.ID_SAVED,
                            )
                        )
                    }
                }
            }
        }

        viewModelScope.launch {
            _uiState.map { Pair(it.isFullscreen, it.canEdit) }
                .distinctUntilChanged()
                .collect { (isFullscreen, canEdit) ->
                    _appUiState.update {
                        it.copy(
                            hideAppBar = isFullscreen,
                            hideBottomNavigation = isFullscreen,
                            fabState = it.fabState.copy(
                                visible = canEdit && !isFullscreen
                            ),
                            isFullscreen = isFullscreen,
                            onToggleFullscreen = ::onToggleFullscreen
                        )
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

    fun onClickTask(activity: XapiActivity) {
        val manifestUrl = activity.manifestUrl ?: return
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                LearningUnitDetail.create(
                    learningUnitManifestUrl = manifestUrl,
                    assignmentActivityId = assignmentActivityId,
                    expectedIdentifier = activity.id,
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
        //Fix me
        _uiState.update { it.copy(isFullscreen = !it.isFullscreen) }
    }
}