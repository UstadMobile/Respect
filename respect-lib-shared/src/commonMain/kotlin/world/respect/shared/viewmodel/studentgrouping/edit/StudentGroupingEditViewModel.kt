package world.respect.shared.viewmodel.studentgrouping.edit

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
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.db.school.ext.fullName
import world.respect.datalayer.school.PersonDataSource
import world.respect.datalayer.school.model.EnrollmentRoleEnum
import world.respect.datalayer.school.model.Person
import world.respect.datalayer.school.xapi.model.VERB_COMPLETED
import world.respect.datalayer.school.xapi.model.XapiAccount
import world.respect.datalayer.school.xapi.model.XapiActor
import world.respect.datalayer.school.xapi.model.XapiAgent
import world.respect.datalayer.school.xapi.model.XapiGroup
import world.respect.datalayer.school.xapi.model.XapiObjectType
import world.respect.datalayer.school.xapi.model.XapiState
import world.respect.datalayer.school.xapi.model.XapiStatement
import world.respect.datalayer.school.xapi.model.XapiVerb
import world.respect.datalayer.shared.paging.EmptyPagingSourceFactory
import world.respect.datalayer.shared.paging.IPagingSourceFactory
import world.respect.datalayer.shared.paging.PagingSourceFactoryHolder
import world.respect.lib.serializers.InstantAsISO8601
import world.respect.libutil.util.time.localDateInCurrentTimeZone
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.account.RespectSessionAndPerson
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.create_group
import world.respect.shared.generated.resources.save
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.StudentGroupingDetail
import world.respect.shared.navigation.StudentGroupingEdit
import world.respect.shared.resources.UiText
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.ActionBarButtonUiState
import java.util.UUID
import kotlin.getValue
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class StudentGroupingEditUiState(
    val selectedAccount: RespectSessionAndPerson? = null,
    val nameError: UiText? = null,
    val students: IPagingSourceFactory<Int, Person> = EmptyPagingSourceFactory(),
    val selectedStudentIds: List<String> = emptyList(),
    val selectedStudentNames: List<String> = emptyList()
)

class StudentGroupingEditViewModel(
    savedStateHandle: SavedStateHandle,
    var respectAccountManager: RespectAccountManager,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {
    override val scope: Scope = respectAccountManager.requireActiveAccountScope()

    private val _uiState = MutableStateFlow(StudentGroupingEditUiState())

    val uiState = _uiState.asStateFlow()

    private val schoolDataSource: SchoolDataSource by inject()

    private val route: StudentGroupingEdit = savedStateHandle.toRoute()

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

    private val studentPagingSource = pagingSourceByRole(EnrollmentRoleEnum.STUDENT)

    init {

        _appUiState.update {
            it.copy(
                title = Res.string.create_group.asUiText(),
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
    }

    @OptIn(ExperimentalUuidApi::class)
    fun onClickSave() {
        val schoolSelfUrl = respectAccountManager.activeAccount?.school?.self
        val groupName =""
        viewModelScope.launch {
            respectAccountManager.selectedAccountAndPersonFlow.collect { accountAndPerson ->
                _uiState.update { prev ->
                    prev.copy(selectedAccount = accountAndPerson)
                }
            }
        }
        val members = _uiState.value.selectedStudentNames.map { studentName ->
            XapiAgent(
                name = studentName,
                objectType = XapiObjectType.Agent,
                account = XapiAccount(
                    name = studentName,
                    homePage = schoolSelfUrl.toString()
                )
            )
        }


        val group = XapiGroup(
            objectType = XapiObjectType.Group,
            account = XapiAccount(
                name = groupName,
                homePage = schoolSelfUrl.toString()
            ),
            member = members
        )

        val actor = XapiAgent(
            name = _uiState.value.selectedAccount?.person?.fullName(),
            objectType = XapiObjectType.Agent,
            account = XapiAccount(
                name = _uiState.value.selectedAccount?.person?.fullName()?:"",
                homePage = schoolSelfUrl.toString()
            )
        )

        val verb = XapiVerb(
            id = VERB_COMPLETED
        )

        val statement= XapiStatement(
            actor = actor,
            verb = verb,
            `object` = group
        )
        launchWithLoadingIndicator {
            schoolDataSource.xapiStatementDataSource.store(listOf(statement))
        }
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                StudentGroupingDetail
            )
        )
    }

    fun onStudentCheckedChange(person: Person, isChecked: Boolean) {
        _uiState.update { prev ->

            val updatedIds = if (isChecked) {
                prev.selectedStudentIds + person.guid
            } else {
                prev.selectedStudentIds - person.guid
            }

            val updatedNames = if (isChecked) {
                prev.selectedStudentNames + person.fullName()
            } else {
                prev.selectedStudentNames - person.fullName()
            }

            prev.copy(
                selectedStudentIds = updatedIds,
                selectedStudentNames = updatedNames
            )
        }
    }
}
