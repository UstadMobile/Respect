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
import world.respect.datalayer.RespectAppDataSource
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.db.school.ext.fullName
import world.respect.datalayer.db.school.ext.isAdminOrTeacher
import world.respect.datalayer.db.school.ext.isStudent
import world.respect.datalayer.ext.dataOrNull
import world.respect.datalayer.school.model.Assignment
import world.respect.datalayer.school.xapi.model.VERB_ASSIGN
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.opds.model.OpdsPublication
import world.respect.lib.xapi.model.VERB_COMPLETED
import world.respect.lib.xapi.resources.XapiStatementsResource.GetStatementParams
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.account.RespectSessionAndPerson
import world.respect.shared.domain.xapi.XapiAssignmentMapper
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.assignment
import world.respect.shared.generated.resources.assignments
import world.respect.shared.navigation.AssignmentDetail
import world.respect.shared.navigation.AssignmentEdit
import world.respect.shared.navigation.NavCommand
import world.respect.shared.util.AssignmentListScreenFilter
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.FabUiState
import kotlin.uuid.ExperimentalUuidApi

data class AssignmentRow(
    val assignment: Assignment,
    val className: String,
    val completedCount: Int = 0,
    val totalCount: Int = 0,
    val isCompleted: Boolean = false
) {
    val uid: String get() = assignment.uid
    val title: String get() = assignment.title
    val deadline get() = assignment.deadline
    val learningUnits get() = assignment.learningUnits
}

data class AssignmentListUiState(
    val assignments: List<AssignmentRow> = emptyList(),
    val learningUnitInfoFlow: (Url) -> Flow<DataLoadState<OpdsPublication>> = { emptyFlow() },
    val selectedFilter: AssignmentListScreenFilter = AssignmentListScreenFilter.ALL,
    val isStudent: Boolean = false,
    val personName: String = "",
    val completedCount: Int = 0,
    val totalCount: Int = 0,
){
    /**
     * Returns the display label for a given filter based on current state counts.
     */
    fun getLabelForFilter(filter: AssignmentListScreenFilter): String {
        return when (filter) {
            AssignmentListScreenFilter.ALL -> filter.displayName
            AssignmentListScreenFilter.COMPLETED -> "${filter.displayName} ($completedCount)"
            AssignmentListScreenFilter.PENDING -> "${filter.displayName} (${totalCount - completedCount})"
        }
    }
}

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

    private var allRows: List<AssignmentRow> = emptyList()

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
            it.copy(learningUnitInfoFlow = ::learningUnitInfoFlowFor)
        }

        viewModelScope.launch {
            accountManager.selectedAccountAndPersonFlow.collect { selectedAcct ->
                val person = selectedAcct?.person
                _uiState.update {
                    it.copy(
                        isStudent = person?.isStudent() == true,
                        personName = person?.fullName() ?: EMPTY_STRING
                    )
                }

                _appUiState.update { prev ->
                    prev.copy(
                        fabState = prev.fabState.copy(
                            visible = person?.isAdminOrTeacher() == true
                        )
                    )
                }

                loadData(selectedAcct)
            }
        }
    }

    private fun loadData(sessionAndPerson: RespectSessionAndPerson?) {
        val schoolUrl = sessionAndPerson?.session?.account?.school?.self?.toString()?.trim()
            ?.removeSuffix(PATH_SEPARATOR) ?: EMPTY_STRING

        viewModelScope.launch {
            val state = schoolDataSource.xapiStatementsResource.get(
                listParams = GetStatementParams(
                    activity = XapiAssignmentMapper.CATEGORY_ASSIGNMENT_RECIPE,
                    relatedActivities = true,
                    verb = VERB_ASSIGN
                ),
                dataLoadParams = DataLoadParams()
            )

            val statements = state.dataOrNull()?.statements ?: emptyList()

            // Map xAPI Statements directly to AssignmentRow UI Model
            allRows = statements.mapNotNull { statement ->
                val assignment = XapiAssignmentMapper.toAssignment(statement) ?: return@mapNotNull null
                AssignmentRow(
                    assignment = assignment,
                    className = statement.actor.name ?: UNKNOWN_CLASS_NAME
                )
            }.distinctBy { it.assignment.uid }
                .sortedByDescending { it.assignment.lastModified }

            updateFilteredRows()
            viewModelScope.launch {
                schoolDataSource.xapiStatementsResource.getAssignmentCompletions(
                    listParams = GetStatementParams(
                        verb = VERB_COMPLETED,
                    )
                ).collect { allResults ->
                    val completionsByAssignment = allResults.groupBy { it.activityId }

                    // Update rows with completion data
                    allRows = allRows.map { row ->
                        val assignmentActivityId = "$schoolUrl$XAPI_ASSIGNMENT_BASE_PATH${row.assignment.uid}"
                        val results = completionsByAssignment[assignmentActivityId] ?: emptyList()
                        val students = results.distinctBy { it.personUid }
                        val completed = students.count { it.completion == true }
                        val total = students.size
                        val isFinished = total > 0 && completed == total
                        row.copy(
                            completedCount = completed,
                            totalCount = total,
                            isCompleted = isFinished
                        )
                    }
                    updateFilteredRows()
                }
            }
        }
    }

    private fun updateFilteredRows() {
        _uiState.update { state ->
            val filtered = allRows.filter { row ->
                when (state.selectedFilter) {
                    AssignmentListScreenFilter.ALL -> true
                    AssignmentListScreenFilter.COMPLETED -> row.isCompleted
                    AssignmentListScreenFilter.PENDING -> !row.isCompleted
                }
            }
            state.copy(
                assignments = filtered,
                completedCount = allRows.count { it.isCompleted },
                totalCount = allRows.size
            )
        }
    }

    fun onFilterChanged(filter: AssignmentListScreenFilter) {
        _uiState.update { it.copy(selectedFilter = filter) }
        updateFilteredRows()
    }

    fun onClickAssignment(assignment: Assignment) {
        _navCommandFlow.tryEmit(NavCommand.Navigate(AssignmentDetail(assignment.uid)))
    }

    fun onClickAdd() {
        _navCommandFlow.tryEmit(NavCommand.Navigate(AssignmentEdit.create(uid = null)))
    }

    fun learningUnitInfoFlowFor(url: Url): Flow<DataLoadState<OpdsPublication>> {
        return schoolDataSource.opdsPublicationDataSource.getByUrlAsFlow(
            url = url, params = DataLoadParams(), null, null
        )
    }

    companion object {
        private const val PATH_SEPARATOR = "/"
        private const val XAPI_ASSIGNMENT_BASE_PATH = "/xapi/activities/assignment/"
        private const val UNKNOWN_CLASS_NAME = "Unknown Class"
        private const val EMPTY_STRING = ""
    }
}
