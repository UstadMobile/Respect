package world.respect.shared.viewmodel.studentgrouping.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.datalayer.SchoolDataSource
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.datalayer.school.PersonDataSource
import world.respect.datalayer.school.model.EnrollmentRoleEnum
import world.respect.datalayer.school.model.Person
import world.respect.datalayer.shared.paging.EmptyPagingSourceFactory
import world.respect.datalayer.shared.paging.IPagingSourceFactory
import world.respect.datalayer.shared.paging.PagingSourceFactoryHolder
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.xapi.model.VERB_SAVED
import world.respect.lib.xapi.model.XapiAccount
import world.respect.lib.xapi.model.XapiActivity
import world.respect.lib.xapi.model.XapiContext
import world.respect.lib.xapi.model.XapiContextActivities
import world.respect.lib.xapi.model.XapiGroup
import world.respect.lib.xapi.model.XapiGroup.Companion.CLASS
import world.respect.lib.xapi.model.XapiGroup.Companion.RESULT_KEY_GROUP_UPDATED
import world.respect.lib.xapi.model.XapiObjectType
import world.respect.lib.xapi.model.XapiStatement
import world.respect.lib.xapi.model.XapiVerb
import world.respect.lib.xapi.resources.XapiStatementsResource
import world.respect.libutil.util.time.localDateInCurrentTimeZone
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.xapi.createVoidingStatement
import world.respect.shared.domain.xapi.toXapiAgent
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.edit_group
import world.respect.shared.generated.resources.create_group
import world.respect.shared.generated.resources.required_field
import world.respect.shared.generated.resources.save
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.StudentGroupingDetail
import world.respect.shared.navigation.StudentGroupingEdit
import world.respect.shared.resources.UiText
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.ActionBarButtonUiState
import kotlin.getValue
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class StudentGroupingEditUiState(
    val groupName: String = "",
    val groupNameError: UiText? = null,
    val students: IPagingSourceFactory<Int, Person> = EmptyPagingSourceFactory(),
    val selectedStudents: List<Person> = emptyList(),
    val statementId: String? = null
) {
    val selectedStudentIds: Set<String>
        get() = selectedStudents.map { it.guid }.toSet()
}

class StudentGroupingEditViewModel(
    savedStateHandle: SavedStateHandle,
    private val respectAccountManager: RespectAccountManager
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = respectAccountManager.requireActiveAccountScope()

    private val _uiState = MutableStateFlow(StudentGroupingEditUiState())

    val uiState = _uiState.asStateFlow()

    private val schoolDataSource: SchoolDataSource by inject()

    private val route: StudentGroupingEdit = savedStateHandle.toRoute()

    private val schoolSelfUrl = respectAccountManager.activeAccount?.school?.self
        ?: throw IllegalStateException("schoolSelfUrl is required")

    val classActivityId = "${schoolSelfUrl}${CLASS}${route.classUid}"

    private fun pagingSourceByRole(): PagingSourceFactoryHolder<Int, Person> {
        return PagingSourceFactoryHolder {
            schoolDataSource.personDataSource.listAsPagingSource(
                loadParams = DataLoadParams(),
                params = PersonDataSource.GetListParams(
                    filterByClazzUid = route.classUid,
                    filterByEnrolmentRole = EnrollmentRoleEnum.STUDENT,
                    inClassOnDay = localDateInCurrentTimeZone(),
                )
            )
        }
    }

    private val studentPagingSource = pagingSourceByRole()

    init {
        _appUiState.update {
            it.copy(
                title = if (route.groupId == null)
                    Res.string.create_group.asUiText()
                else
                    Res.string.edit_group.asUiText(),

                userAccountIconVisible = false,
                hideBottomNavigation = true,
                actionBarButtonState = ActionBarButtonUiState(
                    visible = true,
                    text = Res.string.save.asUiText(),
                    onClick = ::onClickSave
                ),
            )
        }

        _uiState.update {
            it.copy(students = studentPagingSource)
        }

        route.groupId?.let { groupId ->
            @OptIn(ExperimentalUuidApi::class)
            viewModelScope.launch {
                val statementResult = schoolDataSource.xapiStatementsResource.get(
                    listParams = XapiStatementsResource.GetStatementParams(
                        verb = VERB_SAVED,
                        activity = classActivityId,
                        relatedActivities = true,
                    ),
                    dataLoadParams = DataLoadParams()
                ).dataOrNull() ?: return@launch

                // Find the latest statement for this group by sorting by timestamp
                val groupStatement = statementResult.statements
                    .filter { statement ->
                        val group = statement.`object` as? XapiGroup
                        group?.account?.name == groupId
                    }
                    .maxByOrNull {
                        it.timestamp ?: it.stored ?: Instant.DISTANT_PAST
                    }
                    ?: return@launch

                val group = groupStatement.`object` as XapiGroup

                val groupName = group.name
                val statementId = groupStatement.id

                val memberIds = group.member
                    ?.mapNotNull { it.account?.name }
                    ?: emptyList()

                val persons = memberIds.mapNotNull { id ->
                    schoolDataSource.personDataSource.findByGuid(
                        DataLoadParams(),
                        id
                    ).dataOrNull()
                }

                _uiState.update { prev ->
                    prev.copy(
                        groupName = groupName ?: "",
                        selectedStudents = persons,
                        statementId = statementId.toString()
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun onClickSave() {

        val groupName = _uiState.value.groupName
        val existingStatementId = _uiState.value.statementId

        if (groupName.isBlank()) {
            _uiState.update {
                it.copy(groupNameError = Res.string.required_field.asUiText())
            }
            return
        } else {
            _uiState.update { it.copy(groupNameError = null) }
        }
        launchWithLoadingIndicator {

            val person = respectAccountManager.selectedAccountAndPersonFlow.first()?.person
                ?: throw IllegalStateException("No person selected when trying to save group")

            val actor = person.toXapiAgent(schoolSelfUrl.toString())

            if (existingStatementId != null) {
                val voidingStatement = createVoidingStatement(actor, existingStatementId)
                schoolDataSource.xapiStatementsResource.post(listOf(voidingStatement))
            }

            //  Create a new statement with the updated group information
            val members = _uiState.value.selectedStudents.map { student ->
                student.toXapiAgent(schoolSelfUrl.toString())
            }

            val groupId = route.groupId ?: Uuid.random().toString()

            val group = XapiGroup(
                objectType = XapiObjectType.Group,
                name = groupName,
                account = XapiAccount(
                    name = groupId,
                    homePage = schoolSelfUrl.toString()
                ),
                member = members
            )

            val verb = XapiVerb(
                id = VERB_SAVED
            )

            val statement = XapiStatement(
                actor = actor,
                verb = verb,
                `object` = group,
                timestamp = kotlin.time.Clock.System.now(),
                context = XapiContext(
                    contextActivities = XapiContextActivities(
                        parent = listOf(
                            XapiActivity(
                                id = classActivityId,
                                objectType = XapiObjectType.Activity
                            )
                        )
                    )
                )
            )

            schoolDataSource.xapiStatementsResource.post(listOf(statement))

            if (route.groupId == null) {
                _navCommandFlow.tryEmit(
                    NavCommand.Navigate(
                        StudentGroupingDetail(groupId = groupId),
                        popUpTo = route,
                        popUpToInclusive = true
                    )
                )
            } else {
                sendResultAndPop(RESULT_KEY_GROUP_UPDATED, true)
            }

        }
    }

    fun onStudentCheckedChange(person: Person, isChecked: Boolean) {
        _uiState.update { prev ->

            val updated = if (isChecked) {
                prev.selectedStudents + person
            } else {
                prev.selectedStudents.filterNot { it.guid == person.guid }
            }

            prev.copy(selectedStudents = updated)
        }
    }

    fun onGroupNameChanged(name: String) {
        _uiState.update {
            it.copy(
                groupName = name,
                groupNameError = null
            )
        }
    }
}