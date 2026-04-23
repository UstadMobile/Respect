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
import world.respect.datalayer.ext.dataOrNull
import world.respect.datalayer.school.model.Assignment
import world.respect.datalayer.school.model.AssignmentLearningUnitRef
import world.respect.lib.opds.model.OpdsPublication
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.ext.whenSubscribed
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.edit
import world.respect.shared.navigation.AssignmentDetail
import world.respect.shared.navigation.AssignmentEdit
import world.respect.shared.navigation.LearningUnitDetail
import world.respect.shared.navigation.NavCommand
import world.respect.shared.util.ext.asUiText
import world.respect.datalayer.db.school.ext.isAdminOrTeacher
import world.respect.datalayer.db.school.ext.isStudent
import world.respect.datalayer.db.school.ext.fullName
import world.respect.datalayer.school.model.Clazz
import world.respect.shared.util.AssignmentStatusFilter
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.FabUiState

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
    val completion: Map<String, Map<String, Int?>> = emptyMap(), // userId -> (unitId -> percent)
    val selectedStatusFilter: AssignmentStatusFilter = AssignmentStatusFilter.ALL,
    val isFullscreen: Boolean = false,
    val isStudent: Boolean = false,
    val personName: String = "",
    val personGuid: String = ""
)

class AssignmentDetailViewModel(
    savedStateHandle: SavedStateHandle,
    accountManager: RespectAccountManager,
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
                    schoolDataSource.classDataSource.findByGuidAsFlow(
                        guid = assignment.dataOrNull()?.classUid ?: ""
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

        // Dummy users and completion data for gradebook
        val dummyUsers = listOf(
            GradebookUser(id = "1", name = "Micky"),
            GradebookUser(id = "2", name = "Mouse"),
            GradebookUser(id = "3", name = "Jerry"),
            GradebookUser(id = "4", name = "Micky"),
            GradebookUser(id = "5", name = "Mouse"),
            GradebookUser(id = "6", name = "Jerry"),
            GradebookUser(id = "7", name = "Jerry")
        )
        // Dummy units (simulate after assignment is loaded)
        val dummyCompletion = mapOf(
            "1" to mapOf("A" to 90, "B" to 90),
            "2" to mapOf("A" to 50, "B" to 0),
            "3" to mapOf("A" to null, "B" to null),
            "4" to mapOf("A" to 90, "B" to 90),
            "5" to mapOf("A" to 50, "B" to 0),
            "6" to mapOf("A" to null, "B" to null),
            "7" to mapOf("a" to 50, "B" to 0),
            // Template for current student UI demo
            "" to mapOf("A" to 98, "B" to 67, "C" to 50)
        )
        _uiState.update {
            it.copy(
                gradeBookUsers = dummyUsers,
                completion = dummyCompletion
            )
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
