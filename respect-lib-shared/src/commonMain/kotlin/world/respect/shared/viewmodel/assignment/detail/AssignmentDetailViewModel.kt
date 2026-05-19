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
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.db.school.ext.fullName
import world.respect.datalayer.db.school.ext.isAdminOrTeacher
import world.respect.datalayer.db.school.ext.isStudent
import world.respect.datalayer.school.PersonDataSource
import world.respect.datalayer.school.model.AssignmentLearningUnitRef
import world.respect.datalayer.school.model.EnrollmentRoleEnum
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.DataLoadingState
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.lib.dataloadstate.ext.firstOrNotLoaded
import world.respect.lib.dataloadstate.ext.map
import world.respect.lib.opds.model.OpdsPublication
import world.respect.lib.xapi.model.AssignmentResult
import world.respect.lib.xapi.model.VERB_ASSIGN
import world.respect.lib.xapi.model.XapiActivity
import world.respect.lib.xapi.model.XapiStatement
import world.respect.lib.xapi.resources.XapiStatementsResource.GetStatementParams
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.xapi.XapiAssignmentConstants
import world.respect.shared.domain.xapi.XapiDummyDataGenerator
import world.respect.shared.domain.xapi.assignmentClassUid
import world.respect.shared.domain.xapi.assignmentLearningUnits
import world.respect.shared.domain.xapi.assignmentTitle
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
    private val dummyDataGenerator: XapiDummyDataGenerator,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()

    private val route: AssignmentDetail = savedStateHandle.toRoute()

    private val schoolDataSource: SchoolDataSource by inject()

    private val _uiState = MutableStateFlow(AssignmentDetailUiState())

    val uiState = _uiState.asStateFlow()

    private var _canEdit = false

    private val schoolUrl = accountManager.activeAccount?.school?.self


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
                learningUnitInfoFlow = ::learningUnitInfoFlowFor
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
            statementsStream.collect { entity ->
                _uiState.update {
                    it.copy(
                        xApiStatement = entity
                    )
                }
                updateStatusCounts()

                _appUiState.update {
                    it.copy(title = entity.dataOrNull()?.assignmentTitle?.asUiText())
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
            statementsStream.distinctUntilChangedBy { it.dataOrNull()?.assignmentClassUid }
                .collectLatest { statementState ->
                    val xapiStatement = statementState.dataOrNull() ?: return@collectLatest
                    val activityId = (xapiStatement.`object` as? XapiActivity)?.id ?: ""

                    //TODO NEED TO REMOVE THIS

                    // Get all students
                    schoolDataSource.personDataSource.list(
                        loadParams = DataLoadParams(),
                        params = PersonDataSource.GetListParams(
                            filterByClazzUid = xapiStatement.assignmentClassUid,
                            filterByEnrolmentRole = EnrollmentRoleEnum.STUDENT
                        )
                    ).dataOrNull()?.let { students ->

                        // Generate dummy statements using the generator
                        val dummyStatements = dummyDataGenerator.generateDummyStatements(
                            students = students,
                            assignment = xapiStatement,
                            schoolUrl = schoolUrl.toString()
                        )

                        // Post statements
                        schoolDataSource.xapiStatementsResource.post(dummyStatements)

                        // Observe progress
                        schoolDataSource.xapiStatementsResource
                            .getAssignmentResult(
                                assignmentActivityId = activityId,
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
            AssignmentStatusFilter.IN_PROGRESS -> fullList.filter {
                it.completion == false && (it.progress ?: 0) > 0
            }

            AssignmentStatusFilter.NOT_STARTED -> fullList.filter { it.completion == false && (it.progress == null) }
        }
        _uiState.update { it.copy(filteredProgressRow = filtered) }

    }

    private fun updateStatusCounts() {
        val units =
            _uiState.value.xApiStatement.dataOrNull()?.assignmentLearningUnits ?: emptyList()
        val progressByStudent = _uiState.value.assignmentProgressRow.groupBy { it.personUid }

        val statusCounts = progressByStudent.values
            .map { results -> getStudentStatus(results, units) }
            .groupingBy { it }
            .eachCount()
            .toMutableMap()
            .apply { put(AssignmentStatusFilter.ALL, progressByStudent.size) }

        _uiState.update { it.copy(statusCounts = statusCounts) }
    }

    private fun getStudentStatus(
        results: List<AssignmentResult>,
        units: List<AssignmentLearningUnitRef>
    ): AssignmentStatusFilter = when {
        results.size == units.size && units.isNotEmpty() && results.all { it.completion == true } ->
            AssignmentStatusFilter.COMPLETED

        results.any { it.completion == true || (it.progress ?: 0) > 0 } ->
            AssignmentStatusFilter.IN_PROGRESS

        else -> AssignmentStatusFilter.NOT_STARTED
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
            NavCommand.Navigate(
                AssignmentEdit.create(
                    uid = route.uid,
                    assignmentActivityId = route.assignmentActivityId
                )
            )
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
