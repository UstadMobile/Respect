package world.respect.shared.viewmodel.studentgrouping.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.db.school.ext.fullName
import world.respect.datalayer.ext.dataOrNull
import world.respect.datalayer.school.PersonDataSource
import world.respect.datalayer.school.model.EnrollmentRoleEnum
import world.respect.datalayer.school.model.Person
import world.respect.datalayer.shared.paging.EmptyPagingSourceFactory
import world.respect.datalayer.shared.paging.IPagingSourceFactory
import world.respect.datalayer.shared.paging.PagingSourceFactoryHolder
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.xapi.model.VERB_SAVED
import world.respect.lib.xapi.model.VERB_VOIDED
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
import world.respect.lib.xapi.model.XapiStatementRef
import world.respect.lib.xapi.model.XapiVerb
import world.respect.lib.xapi.resources.XapiStatementsResource
import world.respect.libutil.util.time.localDateInCurrentTimeZone
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.account.RespectSessionAndPerson
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
import kotlin.toString
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class StudentGroupingEditUiState(
    val selectedAccount: RespectSessionAndPerson? = null,
    val groupName: String = "",
    val groupNameError: UiText? = null,
    val students: IPagingSourceFactory<Int, Person> = EmptyPagingSourceFactory(),
    val selectedStudents: List<Person> = emptyList(),
    val statementId: String? = null
) {
    val personName: String
        get() = selectedAccount?.person?.fullName() ?: ""

    val personId: String
        get() = selectedAccount?.person?.guid ?: ""

    val selectedStudentIds: Set<String>
        get() = selectedStudents.map { it.guid }.toSet()
}

class StudentGroupingEditViewModel(
    savedStateHandle: SavedStateHandle,
    respectAccountManager: RespectAccountManager
) : RespectViewModel(savedStateHandle), KoinScopeComponent {


    override val scope: Scope = respectAccountManager.requireActiveAccountScope()

    private val _uiState = MutableStateFlow(StudentGroupingEditUiState())
    val uiState = _uiState.asStateFlow()

    private val schoolDataSource: SchoolDataSource by inject()
    private val route: StudentGroupingEdit = savedStateHandle.toRoute()

    val schoolSelfUrl = respectAccountManager.activeAccount?.school?.self
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

                    val groupStatement = statementResult.statements
                        .firstOrNull { statement ->
                            val group = statement.`object` as? XapiGroup
                            group?.account?.name == groupId
                        }
                        ?: return@launch

                    val group = groupStatement.`object` as XapiGroup

                    val groupId = group.account?.name
                    val groupName = group.name
                    val statementId = groupStatement.id?.toString()

                    Napier.d("=== STUDENT GROUPING EDIT DEBUG ===")
                    Napier.d("Group ID: $groupId")
                    Napier.d("Group Name: $groupName")
                    Napier.d("Statement ID: $statementId")
                    Napier.d("===================================")

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
                            statementId = statementId
                        )
                    }
            }
        }
        viewModelScope.launch {
            respectAccountManager.selectedAccountAndPersonFlow.collect { selectedAccount ->
                _uiState.update {
                    it.copy(selectedAccount = selectedAccount)
                }
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun onClickSave() {

        val groupName = _uiState.value.groupName
        val classActivityId = "${schoolSelfUrl}${CLASS}${route.classUid}"
        val existingStatementId = _uiState.value.statementId

        Napier.d("=== STUDENT SAVE GROUP DEBUG ===")
        Napier.d("Group ID from route: ${route.groupId}")
        Napier.d("Group Name: $groupName")
        Napier.d("Existing Statement ID: $existingStatementId")
        Napier.d("========================")

        if (groupName.isBlank()) {
            _uiState.update {
                it.copy(groupNameError = Res.string.required_field.asUiText())
            }
            return
        } else {
            _uiState.update { it.copy(groupNameError = null) }
        }

        viewModelScope.launch {
                // Step 1: If editing an existing group, void the old statement first
                if (existingStatementId != null) {
                    Napier.d("=== VOIDING EXISTING STATEMENT ===")
                    Napier.d("Statement ID to void: $existingStatementId")

                    val actor = XapiAgent(
                        name = _uiState.value.personName,
                        objectType = XapiObjectType.Agent,
                        account = XapiAccount(
                            name = _uiState.value.personId,
                            homePage = schoolSelfUrl.toString()
                        )
                    )

                    val voidVerb = XapiVerb(
                        id = VERB_VOIDED,
                        display = mapOf("en-US" to "voided")
                    )

                    val statementRef = XapiStatementRef(
                        objectType = XapiObjectType.StatementRef,
                        id = existingStatementId
                    )

                    val voidingStatement = XapiStatement(
                        actor = actor,
                        verb = voidVerb,
                        `object` = statementRef,
                        timestamp = Clock.System.now()
                    )

                    schoolDataSource.xapiStatementsResource.post(listOf(voidingStatement))

                    Napier.d("=== VOIDING COMPLETE ===")
                }

                // Step 2: Create a new statement with the updated group information
                val members = _uiState.value.selectedStudents.map { person ->
                    XapiAgent(
                        name = person.fullName(),
                        objectType = XapiObjectType.Agent,
                        account = XapiAccount(
                            name = person.guid,
                            homePage = schoolSelfUrl.toString()
                        )
                    )
                }

                // Always use the same groupId, but create a new statement ID
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

                val actor = XapiAgent(
                    name = _uiState.value.personName,
                    objectType = XapiObjectType.Agent,
                    account = XapiAccount(
                        name = _uiState.value.personId,
                        homePage = schoolSelfUrl.toString()
                    )
                )

                val verbId = VERB_SAVED
                val verb = XapiVerb(
                    id = verbId,
                    display = mapOf("en" to verbId)
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

                schoolDataSource.xapiStatementsResource.post(listOf(statement))

                Napier.d("=== NEW STATEMENT SAVED ===")
                Napier.d("Group ID: $groupId")
                Napier.d("New Statement ID: ${statement.id}")

                if (route.groupId == null) {
                    _navCommandFlow.tryEmit(
                        NavCommand.Navigate(
                            StudentGroupingDetail(groupId, route.classUid, statement.id?.toString()),
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