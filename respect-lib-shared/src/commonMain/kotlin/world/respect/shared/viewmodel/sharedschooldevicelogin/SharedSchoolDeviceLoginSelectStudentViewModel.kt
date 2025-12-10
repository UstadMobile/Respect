package world.respect.shared.viewmodel.sharedschooldevicelogin

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.school.PersonDataSource
import world.respect.datalayer.school.model.EnrollmentRoleEnum
import world.respect.datalayer.school.model.Person
import world.respect.datalayer.shared.paging.EmptyPagingSourceFactory
import world.respect.datalayer.shared.paging.IPagingSourceFactory
import world.respect.datalayer.shared.paging.PagingSourceFactoryHolder
import world.respect.libutil.util.time.localDateInCurrentTimeZone
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.select_student
import world.respect.shared.navigation.EnterRollNumber
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.SelectStudent
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel

data class SelectStudentUiState(
    val loading: Boolean = false,
    val students: IPagingSourceFactory<Int, Person> = EmptyPagingSourceFactory(),

    )
class SharedSchoolDeviceLoginSelectStudentViewModel(
    savedStateHandle: SavedStateHandle,
    accountManager: RespectAccountManager,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {
    override val scope: Scope = accountManager.requireSelectedAccountScope()
    private val _uiState = MutableStateFlow(SelectStudentUiState())
    val uiState: Flow<SelectStudentUiState> = _uiState.asStateFlow()

    private val schoolDataSource: SchoolDataSource by inject()
    private val route: SelectStudent = savedStateHandle.toRoute()

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
    private val studentPagingSource =  pagingSourceByRole(EnrollmentRoleEnum.STUDENT)

    init {
        _appUiState.update { prev ->
            prev.copy(
                title = Res.string.select_student.asUiText(),
                navigationVisible = true,
                hideBottomNavigation = true,
                settingsIconVisible = false
            )
        }
        _uiState.update {
            it.copy(
                students = studentPagingSource
            )
        }
    }

    fun onClickStudent(person: Person) {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(EnterRollNumber)
        )
    }
}