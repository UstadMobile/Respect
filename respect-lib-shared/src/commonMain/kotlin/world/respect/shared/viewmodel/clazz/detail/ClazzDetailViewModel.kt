package world.respect.shared.viewmodel.clazz.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.DataLoadingState
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.ext.dataOrNull
import world.respect.datalayer.school.PersonDataSource
import world.respect.datalayer.school.model.Clazz
import world.respect.datalayer.school.model.EnrollmentRoleEnum
import world.respect.datalayer.school.model.Person
import world.respect.datalayer.shared.paging.EmptyPagingSourceFactory
import world.respect.datalayer.shared.paging.IPagingSourceFactory
import world.respect.datalayer.shared.paging.PagingSourceFactoryHolder
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.account.invite.ApproveOrDeclineInviteRequestUseCase
import world.respect.shared.ext.whenSubscribed
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.first_name
import world.respect.shared.generated.resources.last_name
import world.respect.shared.generated.resources.all
import world.respect.shared.generated.resources.active
import world.respect.shared.generated.resources.edit
import world.respect.shared.navigation.ClazzEdit
import world.respect.shared.navigation.AddPersonToClazz
import world.respect.shared.navigation.ClazzDetail
import world.respect.shared.navigation.NavCommand
import world.respect.shared.util.FilterChipsOption
import world.respect.shared.util.SortOrderOption
import world.respect.shared.util.ext.asUiText
import world.respect.shared.util.ext.isAdminOrTeacher
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.FabUiState
import world.respect.shared.viewmodel.clazz.detail.ClazzDetailViewModel.Companion.ALL
import kotlin.getValue

data class ClazzDetailUiState(
    val teachers: IPagingSourceFactory<Int, Person> = EmptyPagingSourceFactory() ,
    val students: IPagingSourceFactory<Int, Person> = EmptyPagingSourceFactory(),
    val pendingTeachers:IPagingSourceFactory<Int, Person> = EmptyPagingSourceFactory() ,
    val pendingStudents: IPagingSourceFactory<Int, Person> = EmptyPagingSourceFactory() ,

    val listOfPending: List<Person> = emptyList(),
    val chipOptions: List<FilterChipsOption> = emptyList(),
    val selectedChip: String = ALL,
    val sortOptions: List<SortOrderOption> = emptyList(),
    val activeSortOrderOption: SortOrderOption = SortOrderOption(
        Res.string.first_name, 1, true
    ),
    val fieldsEnabled: Boolean = true,
    val clazz: DataLoadState<Clazz> = DataLoadingState(),
    val isPendingExpanded: Boolean = true,
    val isTeachersExpanded: Boolean = true,
    val isStudentsExpanded: Boolean = true,
    val inviteCodePrefix: String? = null,
    val showAddStudent: Boolean = false,
    val showAddTeacher: Boolean = false,
)

class ClazzDetailViewModel(
    savedStateHandle: SavedStateHandle,
    accountManager: RespectAccountManager,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireSelectedAccountScope()

    private val schoolDataSource: SchoolDataSource by inject()

    private val approveOrDeclineInviteRequestUseCase: ApproveOrDeclineInviteRequestUseCase by inject()

    private val _uiState = MutableStateFlow(ClazzDetailUiState())

    val uiState = _uiState.asStateFlow()

    private val route: ClazzDetail = savedStateHandle.toRoute()

    private fun pagingSourceByRole(role: EnrollmentRoleEnum): PagingSourceFactoryHolder<Int, Person> {
        return PagingSourceFactoryHolder {
            schoolDataSource.personDataSource.listAsPagingSource(
                loadParams = DataLoadParams(),
                params = PersonDataSource.GetListParams(
                    filterByClazzUid = route.guid,
                    filterByEnrolmentRole = role,
                )
            )
        }
    }

    private val teacherPagingSource =  pagingSourceByRole(EnrollmentRoleEnum.TEACHER)

    private val studentPagingSource =  pagingSourceByRole(EnrollmentRoleEnum.STUDENT)

    private val teachersPendingPagingSource = pagingSourceByRole(EnrollmentRoleEnum.PENDING_TEACHER)

    private val studentsPendingPagingSource = pagingSourceByRole(EnrollmentRoleEnum.PENDING_STUDENT)

    init {
        _appUiState.update {
            it.copy(
                fabState = FabUiState(
                    visible = true,
                    icon = FabUiState.FabIcon.EDIT,
                    text = Res.string.edit.asUiText(),
                    onClick = ::onClickEdit
                )
            )
        }

        _uiState.update {
            it.copy(
                teachers = teacherPagingSource,
                students = studentPagingSource,
                pendingTeachers = teachersPendingPagingSource,
                pendingStudents = studentsPendingPagingSource,
                sortOptions = listOf(
                    SortOrderOption(
                        fieldMessageId = Res.string.first_name, flag = 1, order = true
                    ), SortOrderOption(
                        fieldMessageId = Res.string.last_name, flag = 2, order = true
                    )
                ),
                chipOptions = listOf(
                    FilterChipsOption(Res.string.all.asUiText()),
                    FilterChipsOption(Res.string.active.asUiText())
                ),
            )
        }

        viewModelScope.launch {
            schoolDataSource.classDataSource.findByGuidAsFlow(route.guid).collect { clazz ->
                _appUiState.update {
                    it.copy(title = clazz.dataOrNull()?.title?.asUiText())
                }
                _uiState.update { it.copy(clazz = clazz) }
            }
        }

        viewModelScope.launch {
            _uiState.whenSubscribed {
                accountManager.selectedAccountAndPersonFlow.collect { selectedAccountAndPerson ->
                    _uiState.update { prev ->
                        prev.copy(
                            showAddStudent = selectedAccountAndPerson?.person?.isAdminOrTeacher() == true,
                            showAddTeacher = selectedAccountAndPerson?.person?.isAdminOrTeacher() == true,
                        )
                    }

                    _appUiState.update {
                        it.copy(
                            fabState = it.fabState.copy(
                                visible = selectedAccountAndPerson?.person?.isAdminOrTeacher() == true
                            )
                        )
                    }
                }
            }
        }
    }

    fun onClickAddPersonToClazz(roleType: EnrollmentRoleEnum) {
        val clazz = _uiState.value.clazz.dataOrNull() ?: return

        val classInviteCode = when(roleType){
            EnrollmentRoleEnum.TEACHER -> clazz.teacherInviteCode
            EnrollmentRoleEnum.STUDENT -> clazz.studentInviteCode
            else -> throw IllegalStateException()
        }

        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                AddPersonToClazz.create(
                    roleType = roleType,
                    inviteCode = classInviteCode,
                )
            )
        )
    }

    fun onSortOrderChanged(sortOption: SortOrderOption) {
        _uiState.update {
            it.copy(activeSortOrderOption = sortOption)
        }
    }

    fun onSelectChip(chip: String) {
        _uiState.update { it.copy(selectedChip = chip) }
    }

    fun onClickAcceptInvite(user: Person) {
        viewModelScope.launch {
            try {
                approveOrDeclineInviteRequestUseCase(
                    personUid = user.guid,
                    classUid = route.guid,
                    approved = true,
                )
            }catch(e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    fun onClickDismissInvite(user: Person) {}


    fun onTogglePendingSection() {
        _uiState.update { it.copy(isPendingExpanded = !it.isPendingExpanded) }
    }

    fun onToggleTeachersSection() {
        _uiState.update { it.copy(isTeachersExpanded = !it.isTeachersExpanded) }
    }

    fun onToggleStudentsSection() {
        _uiState.update { it.copy(isStudentsExpanded = !it.isStudentsExpanded) }
    }

    fun onClickEdit() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(ClazzEdit(route.guid))
        )
    }

    companion object {
        const val ALL = "All"

    }
}
