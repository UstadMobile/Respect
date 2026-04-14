package world.respect.shared.viewmodel.studentgrouping.edit

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.db.school.ext.fullName
import world.respect.datalayer.school.PersonDataSource
import world.respect.datalayer.school.model.EnrollmentRoleEnum
import world.respect.datalayer.school.model.Person
import world.respect.datalayer.school.xapi.model.XapiAccount
import world.respect.datalayer.school.xapi.model.XapiAgent
import world.respect.datalayer.school.xapi.model.XapiObjectType
import world.respect.datalayer.shared.paging.EmptyPagingSourceFactory
import world.respect.datalayer.shared.paging.IPagingSourceFactory
import world.respect.datalayer.shared.paging.PagingSourceFactoryHolder
import world.respect.libutil.util.time.localDateInCurrentTimeZone
import world.respect.shared.domain.account.RespectAccountManager
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
import kotlin.getValue

data class StudentGroupingEditUiState(
    val nameError: UiText? = null,
    val students: IPagingSourceFactory<Int, Person> = EmptyPagingSourceFactory(),
    val selectedStudentIds: List<String> = emptyList(),
    val selectedStudentNames: List<String> = emptyList()
)

class StudentGroupingEditViewModel(
    savedStateHandle: SavedStateHandle,
    var accountManager: RespectAccountManager,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {
    override val scope: Scope = accountManager.requireActiveAccountScope()

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

    fun onClickSave() {
        val schoolSelfUrl = accountManager.activeAccount?.school?.self
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
