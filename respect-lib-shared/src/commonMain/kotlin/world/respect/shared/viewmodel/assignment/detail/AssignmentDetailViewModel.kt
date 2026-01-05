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
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.DataLoadingState
import world.respect.datalayer.RespectAppDataSource
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.ext.dataOrNull
import world.respect.datalayer.school.PersonDataSource
import world.respect.datalayer.school.model.Assignment
import world.respect.datalayer.school.model.AssignmentLearningUnitRef
import world.respect.datalayer.school.model.EnrollmentRoleEnum
import world.respect.datalayer.school.model.Person
import world.respect.datalayer.shared.paging.IPagingSourceFactory
import world.respect.datalayer.shared.paging.PagingSourceFactoryHolder
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
import world.respect.datalayer.school.model.Clazz
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.FabUiState

data class AssignmentDetailUiState(
    val assignment: DataLoadState<Assignment> = DataLoadingState(),
    val assignmentClass: DataLoadState<Clazz> = DataLoadingState(),
    val learningUnitInfoFlow: (Url) -> Flow<DataLoadState<OpdsPublication>> = {
        flowOf(DataLoadingState())
    },
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
                    _appUiState.update {
                        it.copy(
                            fabState = it.fabState.copy(
                                visible = selectedAccount?.person?.isAdminOrTeacher() == true
                            )
                        )
                    }
                }
            }
        }
    }

    fun onClickEdit() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(AssignmentEdit.create(uid = route.uid))
        )
    }

    fun learningUnitInfoFlowFor(url: Url): Flow<DataLoadState<OpdsPublication>> {
        return respectAppDataSource.opdsDataSource.loadOpdsPublication(
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
}