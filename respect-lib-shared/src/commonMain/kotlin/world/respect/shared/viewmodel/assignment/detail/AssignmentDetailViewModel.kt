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
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.DataLoadingState
import world.respect.datalayer.RespectAppDataSource
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.db.school.ext.fullName
import world.respect.datalayer.db.school.ext.isAdminOrTeacher
import world.respect.datalayer.db.school.ext.isStudent
import world.respect.datalayer.ext.dataOrNull
import world.respect.datalayer.ext.firstOrNotLoaded
import world.respect.datalayer.ext.map
import world.respect.datalayer.school.model.Assignment
import world.respect.datalayer.school.model.AssignmentLearningUnitRef
import world.respect.datalayer.school.model.Clazz
import world.respect.datalayer.school.model.EnrollmentRoleEnum
import world.respect.lib.opds.model.OpdsPublication
import world.respect.shared.domain.account.RespectAccountManager
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
import world.respect.lib.xapi.model.AssignmentResult
import world.respect.lib.xapi.model.VERB_ASSIGN
import world.respect.lib.xapi.resources.XapiStatementsResource.GetStatementParams
import world.respect.shared.domain.xapi.XapiAssignmentMapper
import world.respect.shared.domain.xapi.XapiDummyDataGenerator


data class AssignmentDetailUiState(
    val assignment: DataLoadState<Assignment> = DataLoadingState(),
    val assignmentClass: DataLoadState<Clazz> = DataLoadingState(),
    val learningUnitInfoFlow: (Url) -> Flow<DataLoadState<OpdsPublication>> = {
        flowOf(DataLoadingState())
    },
    val assignmentProgressRow: List<AssignmentResult> = emptyList(),
    val statusCounts: Map<AssignmentStatusFilter, Int> = emptyMap(),
    val filteredProgressRow: List<AssignmentResult> = emptyList(),
    val selectedStatusFilter: AssignmentStatusFilter = AssignmentStatusFilter.ALL,
    val isFullscreen: Boolean = false,
    val isStudent: Boolean = false,
    val personName: String = "",
    val personGuid: String = ""
)

@OptIn(ExperimentalUuidApi::class)
class AssignmentDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val accountManager: RespectAccountManager,
    private val respectAppDataSource: RespectAppDataSource,
    private val dummyDataGenerator: XapiDummyDataGenerator,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()

    private val route: AssignmentDetail = savedStateHandle.toRoute()

    private val schoolDataSource: SchoolDataSource by inject()

    private val _uiState = MutableStateFlow(AssignmentDetailUiState())

    val uiState = _uiState.asStateFlow()

    private var _canEdit = false

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
                learningUnitInfoFlow = ::learningUnitInfoFlowFor
            )
        }

        val schoolUrl = accountManager.activeAccount?.school?.self?.toString()?.trim()
            ?.removeSuffix("/")
            ?: ""
        val assignmentActivityId = "$schoolUrl/xapi/activities/assignment/${route.uid}"

        // Load the assignment from xAPI statements
        val assignmentFlow = schoolDataSource.xapiStatementsResource.getAsFlow(
            listParams = GetStatementParams(
                activity = assignmentActivityId,
                verb = VERB_ASSIGN
            ),
            dataLoadParams = DataLoadParams()
        ).map { state ->
            state.map { result ->
                result.statements
                    .mapNotNull { XapiAssignmentMapper.toAssignment(it) }
            }.firstOrNotLoaded()
        }.shareIn(viewModelScope, SharingStarted.Lazily)

        viewModelScope.launch {
            assignmentFlow.collect { entity ->
                _uiState.update {
                    it.copy(assignment = entity)
                }
                updateStatusCounts()

                _appUiState.update {
                    it.copy(title = entity.dataOrNull()?.title?.asUiText())
                }
            }
        }

        viewModelScope.launch {
            assignmentFlow.distinctUntilChangedBy { it.dataOrNull()?.classUid }
                .collectLatest { assignmentState ->
                    val classUid = assignmentState.dataOrNull()?.classUid ?: return@collectLatest
                    schoolDataSource.classDataSource.findByGuidAsFlow(
                        guid = classUid
                    ).collect { assignmentClazz ->
                        _uiState.update { prev ->
                            prev.copy(
                                assignmentClass = assignmentClazz
                            )
                        }
                    }
                }
        }

        viewModelScope.launch {
            _uiState.whenSubscribed {
                accountManager.selectedAccountAndPersonFlow.collect { selectedAccount ->
                    val person = selectedAccount?.person
                    val isStudent = person?.isStudent() == true
                    _canEdit = person?.isAdminOrTeacher() == true

                    _uiState.update {
                        it.copy(
                            isStudent = isStudent,
                            personName = person?.fullName() ?: "",
                            personGuid = person?.guid ?: ""
                        )
                    }
                    updateAppUiState()
                }
            }
        }

        // Load gradebook users and their progress
        viewModelScope.launch {
            assignmentFlow.distinctUntilChangedBy { it.dataOrNull()?.classUid }
                .collectLatest { assignmentState ->
                    val assignment = assignmentState.dataOrNull() ?: return@collectLatest
                    val classUid = assignment.classUid
                    val schoolUrl = accountManager.activeAccount?.school?.self?.toString()?.trim()
                        ?.removeSuffix("/")
                        ?: ""

                    val assignmentActivityId = "$schoolUrl/assignment/${assignment.uid}"

                    //TODO NEED TO REMOVE THIS

                    // Get all students
                    schoolDataSource.personDataSource.list(
                        loadParams = DataLoadParams(),
                        params = world.respect.datalayer.school.PersonDataSource.GetListParams(
                            filterByClazzUid = classUid,
                            filterByEnrolmentRole = EnrollmentRoleEnum.STUDENT
                        )
                    ).dataOrNull()?.let { students ->

                        // Generate dummy statements using the generator
                        val dummyStatements = dummyDataGenerator.generateDummyStatements(
                            students = students,
                            assignment = assignment,
                            schoolUrl = schoolUrl
                        )

                        // Post statements
                        schoolDataSource.xapiStatementsResource.post(dummyStatements)

                        // Observe progress
                        schoolDataSource.xapiStatementsResource
                            .getAssignmentResult(
                                assignmentActivityId = assignmentActivityId,
                            )
                            .collect { progressList ->
                                _uiState.update {
                                    it.copy(assignmentProgressRow = progressList)
                                }
                                updateStatusCounts()
                                updateFilteredProgressRow()
                            }
                    }
                }
        }
    }

    private fun updateFilteredProgressRow() {
        val fullList = _uiState.value.assignmentProgressRow
        val filter = _uiState.value.selectedStatusFilter
        val filtered = when (filter) {
            AssignmentStatusFilter.ALL -> fullList
            AssignmentStatusFilter.COMPLETED -> fullList.filter { it.completion == true }
            AssignmentStatusFilter.IN_PROGRESS -> fullList.filter { it.completion == false && (it.progress ?: 0) > 0 }
            AssignmentStatusFilter.NOT_STARTED -> fullList.filter {it.completion == false && (it.progress== null)}
        }
        _uiState.update { it.copy(filteredProgressRow = filtered) }

    }

    private fun updateStatusCounts() {
        val students = _uiState.value.assignmentProgressRow.distinctBy { it.personUid }
        val progressMap = _uiState.value.assignmentProgressRow.groupBy { it.personUid }
        val units = _uiState.value.assignment.dataOrNull()?.learningUnits ?: emptyList()

        val all = students.size
        var completedCount = 0
        var inProgressCount = 0
        var notStartedCount = 0

        students.forEach { student ->
            val results = progressMap[student.personUid] ?: emptyList()
            val isCompleted =
                results.size == units.size && units.isNotEmpty() && results.all { it.completion == true }
            val isStarted = results.any { it.completion == true || (it.progress ?: 0) > 0 }

            when {
                isCompleted -> completedCount++
                isStarted -> inProgressCount++
                else -> notStartedCount++
            }
        }

        val counts = mapOf(
            AssignmentStatusFilter.ALL to all,
            AssignmentStatusFilter.COMPLETED to completedCount,
            AssignmentStatusFilter.IN_PROGRESS to inProgressCount,
            AssignmentStatusFilter.NOT_STARTED to notStartedCount
        )

        _uiState.update { it.copy(statusCounts = counts) }
    }

    private fun updateAppUiState() {
        val isFullscreen = _uiState.value.isFullscreen
        _appUiState.update {
            it.copy(
                hideAppBar = isFullscreen,
                hideBottomNavigation = isFullscreen,
                fabState = it.fabState.copy(
                    visible = _canEdit && !isFullscreen
                ),
                fullscreenToggleVisible = true,
                isFullscreen = isFullscreen,
                onToggleFullscreen = ::onToggleFullscreen
            )
        }
    }

    fun onClickEdit() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(AssignmentEdit.create(uid = route.uid))
        )
    }

    fun learningUnitInfoFlowFor(url: Url): Flow<DataLoadState<OpdsPublication>> {
        return schoolDataSource.opdsPublicationDataSource.getByUrlAsFlow(
            url = url, params = DataLoadParams(), null, null
        )
    }

    fun onClickLearningUnit(ref: AssignmentLearningUnitRef) {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                LearningUnitDetail.create(
                    learningUnitManifestUrl = ref.learningUnitManifestUrl,
                    appManifestUrl = ref.appManifestUrl,
                )
            )
        )
    }

    fun onStatusFilterChanged(filter: AssignmentStatusFilter) {
        _uiState.update {
            it.copy(selectedStatusFilter = filter)
        }
        updateFilteredProgressRow()
    }

    fun onToggleFullscreen() {
        _uiState.update { it.copy(isFullscreen = !it.isFullscreen) }
        updateAppUiState()
    }
}
