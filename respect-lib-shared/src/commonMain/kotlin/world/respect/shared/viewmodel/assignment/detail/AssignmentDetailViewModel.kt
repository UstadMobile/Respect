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
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.DataLoadingState
import world.respect.datalayer.RespectAppDataSource
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.db.school.ext.fullName
import world.respect.datalayer.db.school.ext.isAdminOrTeacher
import world.respect.datalayer.db.school.ext.isStudent
import world.respect.datalayer.ext.dataOrNull
import world.respect.datalayer.school.model.Assignment
import world.respect.datalayer.school.model.AssignmentLearningUnitRef
import world.respect.datalayer.school.model.Clazz
import world.respect.datalayer.school.model.EnrollmentRoleEnum
import world.respect.lib.opds.model.OpdsPublication
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
import kotlin.uuid.Uuid
import world.respect.lib.xapi.model.AssignmentResult
import kotlin.time.Clock

data class GradebookUser(
    val id: String,
    val name: String,
    val avatarUrl: String? = null
)

data class AssignmentDetailUiState(
    val assignment: DataLoadState<Assignment> = DataLoadingState(),
    val assignmentClass: DataLoadState<Clazz> = DataLoadingState(),
    val learningUnitInfoFlow: (Url) -> Flow<DataLoadState<OpdsPublication>> = {
        flowOf(DataLoadingState())
    },
    val gradeBookUsers: List<GradebookUser> = emptyList(),
    val assignmentProgressRow: List<AssignmentResult> = emptyList(),
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

        val assignmentFlow = schoolDataSource.assignmentDataSource.findByGuidAsFlow(route.uid)
            .shareIn(viewModelScope, SharingStarted.Lazily)

        viewModelScope.launch {
            assignmentFlow.collect { entity ->
                _uiState.update {
                    it.copy(assignment = entity)
                }

                _appUiState.update {
                    it.copy(title = entity.dataOrNull()?.title?.asUiText())
                }
            }
        }

        viewModelScope.launch {
            assignmentFlow.distinctUntilChangedBy { it.dataOrNull()?.classUid }
                .collectLatest { assignment ->
                    val classUid = assignment.dataOrNull()?.classUid ?: return@collectLatest
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
                    val schoolUrl = accountManager.activeAccount?.school?.self
                        ?.toString()
                        ?.trim()
                        ?.removeSuffix("/")
                        ?: ""

                    val assignmentActivityId = "$schoolUrl/assignment/${assignment.uid}"

                    // Get all students
                    schoolDataSource.personDataSource.list(
                        loadParams = DataLoadParams(),
                        params = world.respect.datalayer.school.PersonDataSource.GetListParams(
                            filterByClazzUid = classUid,
                            filterByEnrolmentRole = EnrollmentRoleEnum.STUDENT
                        )
                    ).dataOrNull()?.let { students ->

                        // Update UI users
                        val users = students.map {
                            GradebookUser(
                                id = it.guid,
                                name = it.fullName(),
                            )
                        }

                        _uiState.update {
                            it.copy(gradeBookUsers = users)
                        }

                        // TODO NEED TO CHANGE DUMMY DATAS
                        // Create dummy statements for each student and learning unit
                        val dummyStatements = students.flatMap { student ->
                            assignment.learningUnits.map { ref ->
                                XapiStatement(
                                    id = Uuid.random(),
                                    actor = XapiAgent(
                                        name = student.fullName(),
                                        account = XapiAccount(
                                            homePage = schoolUrl,
                                            name = student.fullName()
                                        )
                                    ),
                                    verb = XapiVerb(
                                        id = "http://adlnet.gov/expapi/verbs/completed",
                                        display = mapOf("en-US" to "completed")
                                    ),
                                    `object` = XapiActivity(
                                        id = ref.learningUnitManifestUrl.toString(),
                                        definition = XapiActivityDefinition(
                                            name = mapOf("en-US" to "Learning Unit"),
                                            description = mapOf(
                                                "en-US" to "A learning unit in the assignment"
                                            )
                                        )
                                    ),
                                    result = XapiResult(
                                        score = XapiResult.Score(
                                            scaled = 0.95F,
                                            raw = 95.0F,
                                            min = 0.0F,
                                            max = 100.0F
                                        ),
                                        success = true,
                                        completion = true,
                                        duration = null
                                    ),
                                    context = XapiContext(
                                        contextActivities = XapiContextActivities(
                                            grouping = listOf(
                                                XapiActivity(
                                                    id = assignmentActivityId,
                                                    objectType = XapiObjectType.Activity
                                                )
                                            )
                                        )
                                    ),
                                    timestamp = Clock.System.now(),
                                    stored = Clock.System.now()
                                )
                            }
                        }

                        // Post statements
                        schoolDataSource.xapiStatementsResource.post(dummyStatements)

                        // Observe progress
                        schoolDataSource.xapiStatementsResource
                            .getAssignmentResult(
                                assignmentActivityId = assignmentActivityId,
                                personUids = students.map { it.guid.toLong() }
                            )
                            .collect { progressList ->
                                _uiState.update {
                                    it.copy(assignmentProgressRow = progressList)
                                }
                            }
                    }
                }
        }

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
    }

    fun onToggleFullscreen() {
        _uiState.update { it.copy(isFullscreen = !it.isFullscreen) }
        updateAppUiState()
    }
}
