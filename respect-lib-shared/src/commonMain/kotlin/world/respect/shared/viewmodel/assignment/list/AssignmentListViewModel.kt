package world.respect.shared.viewmodel.assignment.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.datalayer.RespectAppDataSource
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.db.school.ext.fullName
import world.respect.datalayer.db.school.ext.isAdminOrTeacher
import world.respect.datalayer.db.school.ext.isStudent
import world.respect.datalayer.ext.dataOrNull
import world.respect.datalayer.school.model.Assignment
import world.respect.datalayer.school.xapi.model.VERB_ASSIGN
import world.respect.lib.opds.model.OpdsPublication
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.account.RespectSessionAndPerson
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.assignment
import world.respect.shared.generated.resources.assignments
import world.respect.shared.navigation.AssignmentDetail
import world.respect.shared.navigation.AssignmentEdit
import world.respect.shared.navigation.NavCommand
import world.respect.shared.util.AssignmentFilter
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.FabUiState
import world.respect.lib.xapi.model.AssignmentResult
import world.respect.lib.xapi.resources.XapiStatementsResource.GetStatementParams
import world.respect.shared.domain.xapi.XapiAssignmentMapper
import kotlin.collections.filter
import kotlin.uuid.ExperimentalUuidApi

data class AssignmentListUiState(
    val assignments: List<Assignment> = emptyList(),
    val learningUnitInfoFlow: (Url) -> Flow<DataLoadState<OpdsPublication>> = { emptyFlow() },
    val selectedFilter: AssignmentFilter = AssignmentFilter.ALL,
    val completedCount: Int = 0,
    val totalCount: Int = 0,
    val className: String = "Class 1",
    val isStudent: Boolean = false,
    val personName: String = "",
    val assignmentProgressRow: List<AssignmentResult> = emptyList(),
)

@OptIn(ExperimentalUuidApi::class)
class AssignmentListViewModel(
    savedStateHandle: SavedStateHandle,
    private val accountManager: RespectAccountManager,
    private val respectAppDataSource: RespectAppDataSource,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()

    private val schoolDataSource: SchoolDataSource by inject()

    private val _uiState = MutableStateFlow(AssignmentListUiState())

    val uiState: StateFlow<AssignmentListUiState> = _uiState.asStateFlow()

    private var allAssignments: List<Assignment> = emptyList()

    init {
        _appUiState.update {
            it.copy(
                title = Res.string.assignments.asUiText(),
                fabState = FabUiState(
                    text = Res.string.assignment.asUiText(),
                    icon = FabUiState.FabIcon.ADD,
                    onClick = ::onClickAdd
                ),
                showBackButton = false,
            )
        }

        _uiState.update {
            it.copy(
                learningUnitInfoFlow = ::learningUnitInfoFlowFor,
            )
        }

        viewModelScope.launch {
            accountManager.selectedAccountAndPersonFlow.collect { selectedAcct ->
                val person = selectedAcct?.person
                val isStudent = person?.isStudent() == true
                val personName = person?.fullName() ?: ""

                _uiState.update {
                    it.copy(
                        isStudent = isStudent,
                        personName = personName
                    )
                }

                _appUiState.update { prev ->
                    prev.copy(
                        fabState = prev.fabState.copy(
                            visible = selectedAcct?.person?.isAdminOrTeacher() == true
                        )
                    )
                }

                loadData(selectedAcct)
            }
        }
    }

    private fun loadData(sessionAndPerson: RespectSessionAndPerson?) {
        val schoolUrl = sessionAndPerson?.session?.account?.school?.self.toString()

        viewModelScope.launch {
            val state = schoolDataSource.xapiStatementsResource.get(
                listParams = GetStatementParams(
                    activity = XapiAssignmentMapper.CATEGORY_ASSIGNMENT_RECIPE,
                    relatedActivities = true,
                    verb = VERB_ASSIGN
                ),
                dataLoadParams = DataLoadParams()
            )
            allAssignments = state.dataOrNull()?.statements
                ?.mapNotNull { XapiAssignmentMapper.toAssignment(it) }
                ?.distinctBy { it.uid }
                ?.sortedByDescending { it.lastModified }
                ?: emptyList()

            updateFilteredAssignments()

            allAssignments.forEach { assignment ->
                val assignmentActivityId = "$schoolUrl/xapi/activities/assignment/${assignment.uid}"

                viewModelScope.launch {
                    schoolDataSource.xapiStatementsResource
                        .getAssignmentResult(assignmentActivityId)
                        .collect { results ->
                            _uiState.update { state ->
                                state.copy(
                                    assignmentProgressRow = results,
                                )
                            }
                            val students =
                                uiState.value.assignmentProgressRow.distinctBy { it.personUid }
                            _uiState.update { state ->
                                state.copy(
                                    completedCount = students.count { it.completion == true },
                                    totalCount = students.size,
                                )
                            }
                        }
                }
            }
        }
    }

    private fun updateFilteredAssignments() {
        _uiState.update { state ->
            val filtered = allAssignments.filter { assignment ->
                val results = state.assignmentProgressRow.filter { result ->
                    result.activityId.contains(assignment.uid)
                }
                val isCompleted = results.any { it.completion == true }

                when (state.selectedFilter) {
                    AssignmentFilter.ALL -> true
                    AssignmentFilter.COMPLETED -> isCompleted
                    AssignmentFilter.PENDING -> !isCompleted
                }
            }
            state.copy(assignments = filtered)
        }
    }


    fun onFilterChanged(filter: AssignmentFilter) {
        _uiState.update { it.copy(selectedFilter = filter) }
        updateFilteredAssignments()
    }

    fun onClickAssignment(assignment: Assignment) {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                AssignmentDetail(assignment.uid)
            )
        )
    }

    fun onClickAdd() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                AssignmentEdit.create(uid = null)
            )
        )
    }

    fun learningUnitInfoFlowFor(url: Url): Flow<DataLoadState<OpdsPublication>> {
        return schoolDataSource.opdsPublicationDataSource.getByUrlAsFlow(
            url = url, params = DataLoadParams(), null, null
        )
    }
}
