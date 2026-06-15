package world.respect.shared.viewmodel.studentgrouping.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.datalayer.SchoolDataSource
import io.github.aakira.napier.Napier
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
import world.respect.lib.xapi.model.XapiAgent
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
import world.respect.datalayer.db.school.ext.fullName
import world.respect.lib.xapi.model.VERB_VOIDED
import world.respect.lib.xapi.model.XapiStatementRef
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
import kotlin.time.Clock
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
                try {
                    val statementResult = schoolDataSource.xapiResource.statements.get(
                        listParams = XapiStatementsResource.GetStatementParams(
                            verb = VERB_SAVED,
                            activity = classActivityId,
                            relatedActivities = true,
                        ),
                        dataLoadParams = DataLoadParams()
                    ).dataOrNull()
                        ?: throw IllegalStateException(
                            "Failed to load statements for classActivityId=$classActivityId"
                        )

                    val groupStatement = statementResult.statements
                        .filter { statement ->
                            val group = statement.`object` as? XapiGroup
                            group?.account?.name == groupId
                        }
                        .maxByOrNull {
                            it.timestamp ?: it.stored ?: Instant.DISTANT_PAST
                        }
                        ?: throw IllegalStateException(
                            "Could not find statement for groupId=$groupId"
                        )

                val group = groupStatement.`object` as XapiGroup

                val groupName = group.name
                val statementId = groupStatement.id
                if (statementId == null) {
                    Napier.e("StudentGroupingEditViewModel: group statement id is null for groupId=$groupId, cannot edit")
                    return@launch
                }

                val memberIds = group.member
                    ?.mapNotNull { agent ->
                        val name = agent.account?.name
                        if (name == null) {
                            Napier.w("StudentGroupingEditViewModel: member agent has no account name")
                        }
                        name
                    }
                    ?: emptyList()

                val persons = memberIds.mapNotNull { id ->
                    val person = schoolDataSource.personDataSource.findByGuid(
                        DataLoadParams(),
                        id
                    ).dataOrNull()
                    if (person == null) {
                        Napier.w("StudentGroupingEditViewModel: could not find person with guid=$id")
                    }
                    person
                }

                _uiState.update { prev ->
                    prev.copy(
                        groupName = groupName ?: "",
                        selectedStudents = persons,
                        statementId = statementId.toString()
                    )
                }
                } catch (e: Exception) {
                    Napier.e("StudentGroupingEditViewModel: error loading group", throwable = e)
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

            val sessionAndPerson = respectAccountManager.selectedAccountAndPersonFlow.firstOrNull()
                ?: throw IllegalStateException("No person selected when trying to save group")

            val actor = sessionAndPerson.xapiAgent

            if (existingStatementId != null) {
                val voidingStatement = XapiStatement(
                    actor = actor,
                    verb = XapiVerb(id = VERB_VOIDED),
                    `object` = XapiStatementRef(
                        objectType = XapiObjectType.StatementRef,
                        id = existingStatementId
                    ),
                    timestamp = Clock.System.now()
                )
                schoolDataSource.xapiResource.statements.post(listOf(voidingStatement))
            }

            val members = _uiState.value.selectedStudents.map { student ->
                XapiAgent(
                    name = student.fullName(),
                    objectType = XapiObjectType.Agent,
                    account = XapiAccount(
                        name = student.guid,
                        homePage = schoolSelfUrl.toString()
                    )
                )
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
                timestamp = Clock.System.now(),
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

            schoolDataSource.xapiResource.statements.post(listOf(statement))

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

            prev.copy(
                selectedStudents = updated
            )
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