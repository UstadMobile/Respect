package world.respect.shared.viewmodel.clazz.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.DataLoadingState
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.ext.dataOrNull
import world.respect.datalayer.school.EnrollmentDataSource
import world.respect.datalayer.school.PersonDataSource
import world.respect.datalayer.school.model.Clazz
import world.respect.datalayer.school.model.Enrollment
import world.respect.datalayer.school.model.EnrollmentRoleEnum
import world.respect.datalayer.school.model.Person
import world.respect.datalayer.school.model.StatusEnum
import world.respect.datalayer.shared.paging.EmptyPagingSourceFactory
import world.respect.datalayer.shared.paging.IPagingSourceFactory
import world.respect.datalayer.shared.paging.PagingSourceFactoryHolder
import world.respect.libutil.util.time.localDateInCurrentTimeZone
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.account.invite.ApproveOrDeclineInviteRequestUseCase
import world.respect.shared.domain.school.SchoolPrimaryKeyGenerator
import world.respect.shared.ext.whenSubscribed
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.first_name
import world.respect.shared.generated.resources.last_name
import world.respect.shared.generated.resources.all
import world.respect.shared.generated.resources.active
import world.respect.shared.generated.resources.edit
import world.respect.shared.navigation.ClazzEdit
import world.respect.shared.navigation.ClazzDetail
import world.respect.shared.navigation.EnrollmentList
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.NavResultReturner
import world.respect.shared.navigation.PersonDetail
import world.respect.shared.navigation.PersonList
import world.respect.shared.navigation.RouteResultDest
import world.respect.shared.util.FilterChipsOption
import world.respect.shared.util.SortOrderOption
import world.respect.shared.util.exception.getUiTextOrGeneric
import world.respect.shared.util.ext.asUiText
import world.respect.datalayer.db.school.ext.isAdminOrTeacher
import world.respect.datalayer.school.model.ClassInvite
import world.respect.datalayer.school.model.ClassInviteModeEnum
import world.respect.datalayer.school.writequeue.EnqueueRunPullSyncUseCase
import world.respect.shared.navigation.StudentGroupingDetail
import world.respect.shared.navigation.StudentGroupingEdit
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.FabUiState
import world.respect.shared.viewmodel.app.appstate.Snack
import world.respect.shared.viewmodel.app.appstate.SnackBarDispatcher
import world.respect.shared.viewmodel.clazz.detail.ClazzDetailViewModel.Companion.ALL
import kotlin.time.Clock
import world.respect.lib.xapi.model.VERB_SAVED
import world.respect.lib.xapi.model.XapiGroup
import world.respect.lib.xapi.resources.XapiStatementsResource
import world.respect.lib.xapi.model.XapiGroup.Companion.CLASS

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
    val showStudentGrouping: Boolean = false,
    val isStudentGroupingExpanded: Boolean = true,
    val groupIds: List<String> = emptyList(),
    val groups: List<GroupDisplayData> = emptyList(),
    val statementId: String? = null
)

data class GroupDisplayData(
    val groupId: String,
    val groupName: String,
    val memberCount: Int,
    val memberNames: List<String> = emptyList(),
    val statementId: String? = null
)

class ClazzDetailViewModel(
    savedStateHandle: SavedStateHandle,
    accountManager: RespectAccountManager,
    private val resultReturner: NavResultReturner,
    private val snackBarDispatcher: SnackBarDispatcher,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()

    private val schoolDataSource: SchoolDataSource by inject()

    private val approveOrDeclineInviteRequestUseCase: ApproveOrDeclineInviteRequestUseCase by inject()

    private val schoolPrimaryKeyGenerator: SchoolPrimaryKeyGenerator by inject()

    private val _uiState = MutableStateFlow(ClazzDetailUiState())

    val uiState = _uiState.asStateFlow()

    private val route: ClazzDetail = savedStateHandle.toRoute()

    val schoolSelfUrl = accountManager.activeAccount?.school?.self?.toString()

    val classActivityId = "${schoolSelfUrl}${CLASS}${route.guid}"

    private fun pagingSourceByRole(role: EnrollmentRoleEnum): PagingSourceFactoryHolder<Int, Person> {
        return PagingSourceFactoryHolder {
            schoolDataSource.personDataSource.listAsPagingSource(
                loadParams = DataLoadParams(),
                params = PersonDataSource.GetListParams(
                    filterByClazzUid = route.guid,
                    filterByEnrolmentRole = role,
                    inClassOnDay = localDateInCurrentTimeZone(),
                )
            )
        }
    }

    private val teacherPagingSource =  pagingSourceByRole(EnrollmentRoleEnum.TEACHER)

    private val studentPagingSource =  pagingSourceByRole(EnrollmentRoleEnum.STUDENT)

    private val teachersPendingPagingSource = pagingSourceByRole(EnrollmentRoleEnum.PENDING_TEACHER)

    private val studentsPendingPagingSource = pagingSourceByRole(EnrollmentRoleEnum.PENDING_STUDENT)

    private val enqueuePullSyncUseCase: EnqueueRunPullSyncUseCase by inject()

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
            enqueuePullSyncUseCase()
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
            observeGroupsFromXapi()
        }

        viewModelScope.launch {
            _uiState.whenSubscribed {
                accountManager.selectedAccountAndPersonFlow.collect { selectedAccountAndPerson ->
                    _uiState.update { prev ->
                        prev.copy(
                            showAddStudent = selectedAccountAndPerson?.person?.isAdminOrTeacher() == true,
                            showAddTeacher = selectedAccountAndPerson?.person?.isAdminOrTeacher() == true,
                            showStudentGrouping = selectedAccountAndPerson?.person?.isAdminOrTeacher() == true
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


        listOf(EnrollmentRoleEnum.TEACHER, EnrollmentRoleEnum.STUDENT).forEach { enrolmentRole ->
            viewModelScope.launch {
                resultReturner.filteredResultFlowForKey(
                    "$RESULT_KEY_PREFIX${enrolmentRole.value}"
                ).collect { navResult ->
                    val personToEnrol = navResult.result as? Person ?: return@collect

                    try {
                        schoolDataSource.enrollmentDataSource.store(
                            listOf(
                                Enrollment(
                                    uid = schoolPrimaryKeyGenerator.primaryKeyGenerator.nextId(
                                        Enrollment.TABLE_ID
                                    ).toString(),
                                    classUid = route.guid,
                                    role = enrolmentRole,
                                    personUid = personToEnrol.guid,
                                    beginDate = Clock.System.now().toLocalDateTime(
                                        TimeZone.currentSystemDefault()
                                    ).date,
                                )
                            )
                        )
                    }catch(e: Throwable) {
                        e.printStackTrace()
                    }
                }
            }
        }

    }

    fun onClickAddPersonToClazz(roleType: EnrollmentRoleEnum) {
        viewModelScope.launch {
            val clazz = _uiState.value.clazz.dataOrNull() ?: return@launch

            _navCommandFlow.tryEmit(
                NavCommand.Navigate(
                    PersonList.create(
                        isTopLevel = false,
                        resultDest = RouteResultDest(
                            resultKey = "$RESULT_KEY_PREFIX${roleType.value}",
                            resultPopUpTo = route,
                        ),
                        inviteUid = ClassInvite.uidFor(
                            route.guid, roleType, ClassInviteModeEnum.DIRECT
                        ),
                        classUid = clazz.guid,
                        className = clazz.title,
                        role = roleType,
                    )
                )
            )
         }
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

    fun onToggleStudentGroupingSection() {
        _uiState.update { it.copy(isStudentGroupingExpanded = !it.isStudentGroupingExpanded) }
    }

    fun onClickEdit() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(ClazzEdit(route.guid))
        )
    }

    fun onClickGroup(groupId: String) {
        val groupData = _uiState.value.groups.find { it.groupId == groupId }
        val statementId = groupData?.statementId

        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                StudentGroupingDetail(
                    groupId = groupId,
                    classId = route.guid,
                    statementId = statementId
                )
            )
        )
    }

    fun onClickRemovePersonFromClass(person: Person, role: EnrollmentRoleEnum) {
        viewModelScope.launch {
            try {
                val personEnrollments = schoolDataSource.enrollmentDataSource.list(
                    loadParams = DataLoadParams(),
                    listParams = EnrollmentDataSource.GetListParams(
                        personUid = person.guid,
                        classUid = route.guid,
                    )
                ).dataOrNull() ?: throw IllegalStateException()

                val today = localDateInCurrentTimeZone()
                val modTime = Clock.System.now()

                val enrollmentsToStore = personEnrollments.filter {
                    val endDate = it.endDate

                    it.removedAt == null && (endDate == null || endDate >= today)
                }.map {
                    it.copy(
                        lastModified = modTime,
                        status = if(it.beginDate == today) {
                            StatusEnum.TO_BE_DELETED //probably was just added by mistake
                        }else {
                            it.status
                        },
                        endDate = today,
                        removedAt = modTime,
                    )
                }

                schoolDataSource.enrollmentDataSource.store(enrollmentsToStore)

            }catch(e: Throwable) {
                //do something
                Napier.e("onClickRemovePersonFromClass ERROR", throwable = e)
                snackBarDispatcher.showSnackBar(Snack(e.getUiTextOrGeneric()))
            }
        }
    }

    fun onClickPerson(person: Person) {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                PersonDetail(guid = person.guid)
            )
        )
    }

    fun onClickCreateGroup() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                StudentGroupingEdit(
                    classUid = route.guid, groupId = null
                )
            )
        )
    }

    fun onClickManageEnrollments(person: Person, role: EnrollmentRoleEnum) {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                EnrollmentList.create(
                    filterByPersonUid = person.guid,
                    role = role,
                    filterByClassUid = route.guid
                )
            )
        )
    }

    @OptIn(kotlin.uuid.ExperimentalUuidApi::class)
    private suspend fun observeGroupsFromXapi() {
        schoolDataSource.xapiStatementsResource.getAsFlow(
            listParams = XapiStatementsResource.GetStatementParams(
                verb = VERB_SAVED,
                activity = classActivityId,
                relatedActivities = true,
            ),
            dataLoadParams = DataLoadParams()
        ).collect { dataLoadState ->

                val statementResult = dataLoadState.dataOrNull() ?: return@collect

                // Sort by timestamp descending (latest first) and take first per groupId
                // This ensures we get the latest version of each group
                val groupIdToStatement = statementResult.statements
                    .filter { it.verb.id == VERB_SAVED }
                    .sortedByDescending { it.timestamp ?: it.stored }
                    .mapNotNull { statement ->
                        val group = statement.`object` as? XapiGroup
                        val groupId = group?.account?.name
                        if (groupId != null) {
                            groupId to statement
                        } else {
                            null
                        }
                    }
                    .distinctBy { it.first }
                    .toMap()

                val groupDisplayDataList = groupIdToStatement.map { (groupId, statement) ->
                    val group = statement.`object` as XapiGroup
                    val memberNames = group.member?.mapNotNull { it.name } ?: emptyList()
                    val statementId = statement.id?.toString()

                    GroupDisplayData(
                        groupId = groupId,
                        groupName = group.name ?: "",
                        memberCount = memberNames.size,
                        memberNames = memberNames,
                        statementId = statementId
                    )
                }

                _uiState.update { prev ->
                    prev.copy(groups = groupDisplayDataList)
                }
        }
    }

    companion object {
        const val ALL = "All"

        const val RESULT_KEY_PREFIX = "result_"

    }
}
