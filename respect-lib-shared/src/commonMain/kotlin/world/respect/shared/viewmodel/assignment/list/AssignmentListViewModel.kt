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
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.RespectAppDataSource
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.db.school.ext.fullName
import world.respect.datalayer.db.school.ext.isAdminOrTeacher
import world.respect.datalayer.db.school.ext.isStudent
import world.respect.datalayer.ext.dataOrNull
import world.respect.datalayer.school.AssignmentDataSource
import world.respect.datalayer.school.model.Assignment
import world.respect.datalayer.shared.paging.EmptyPagingSourceFactory
import world.respect.datalayer.shared.paging.IPagingSourceFactory
import world.respect.datalayer.shared.paging.PagingSourceFactoryHolder
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
import world.respect.datalayer.school.model.EnrollmentRoleEnum
import world.respect.lib.xapi.model.AssignmentResult
import world.respect.lib.xapi.model.XapiAccount
import world.respect.lib.xapi.model.XapiActivity
import world.respect.lib.xapi.model.XapiActivityDefinition
import world.respect.lib.xapi.model.XapiAgent
import world.respect.lib.xapi.model.XapiContext
import world.respect.lib.xapi.model.XapiContextActivities
import world.respect.lib.xapi.model.XapiObjectType
import world.respect.lib.xapi.model.XapiResult
import world.respect.lib.xapi.model.XapiStatement
import world.respect.lib.xapi.model.XapiVerb
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.time.Clock


data class AssignmentListUiState(
    val assignments: IPagingSourceFactory<Int, Assignment> = EmptyPagingSourceFactory(),
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

    private val _selectedFilter = MutableStateFlow(AssignmentFilter.ALL)

    private val pagingSourceHolder = PagingSourceFactoryHolder {
        val status = when (_selectedFilter.value) {
            AssignmentFilter.PENDING -> "pending"
            AssignmentFilter.COMPLETED -> "completed"
            else -> null
        }

        schoolDataSource.assignmentDataSource.listAsPagingSource(
            loadParams = DataLoadParams(),
            params = AssignmentDataSource.GetListParams(status = status)
        )
    }

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
                assignments = pagingSourceHolder,
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

                loadProgressData(selectedAcct)
            }
        }

    }

    private fun loadProgressData(sessionAndPerson: RespectSessionAndPerson?) {
        val schoolUrl = sessionAndPerson?.session?.account?.school?.self
            .toString()
            .trim()
            .removeSuffix("/")

        viewModelScope.launch {
            val assignmentsResult = schoolDataSource.assignmentDataSource.list(DataLoadParams())
            val allAssignments = assignmentsResult.dataOrNull() ?: return@launch

            allAssignments.forEach { assignment ->
                val assignmentActivityId = "$schoolUrl/assignment/${assignment.uid}"

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


    fun onFilterChanged(filter: AssignmentFilter) {
        _selectedFilter.value = filter
        _uiState.update { it.copy(selectedFilter = filter) }
        pagingSourceHolder.invalidate()
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
