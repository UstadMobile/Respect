package world.respect.shared.viewmodel.enrollment.list

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
import world.respect.datalayer.school.EnrollmentDataSource
import world.respect.datalayer.school.model.Enrollment
import world.respect.datalayer.school.model.EnrollmentRoleEnum
import world.respect.datalayer.shared.paging.EmptyPagingSourceFactory
import world.respect.datalayer.shared.paging.IPagingSourceFactory
import world.respect.datalayer.shared.paging.PagingSourceFactoryHolder
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.navigation.EnrollmentEdit
import world.respect.shared.navigation.EnrollmentList
import world.respect.shared.navigation.NavCommand
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel

data class EnrollmentListUiState(
    val enrollments: IPagingSourceFactory<Int, Enrollment> = EmptyPagingSourceFactory()
)

class EnrollmentListViewModel(
    savedStateHandle: SavedStateHandle,
    accountManager: RespectAccountManager
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireSelectedAccountScope()

    private val _uiState = MutableStateFlow(EnrollmentListUiState())
    val uiState = _uiState.asStateFlow()

    private val schoolDataSource: SchoolDataSource by inject()
    private val route: EnrollmentList = savedStateHandle.toRoute()

    private val pagingSourceHolder = PagingSourceFactoryHolder {
        schoolDataSource.enrollmentDataSource.listAsPagingSource(
            loadParams = DataLoadParams(),
            listParams = EnrollmentDataSource.GetListParams(
                classUid = route.clazzGuid,
                personUid = route.personGuid,
                role = EnrollmentRoleEnum.valueOf(route.role)
            )
        )
    }

    init {

        _appUiState.update {
            it.copy(
                title= (route.role).asUiText()
            )
        }
        _uiState.update {
            it.copy(enrollments = pagingSourceHolder)
        }
    }

    fun onEditEnrollment(enrollment: Enrollment?) {
        if(enrollment!=null) {
            _navCommandFlow.tryEmit(
                NavCommand.Navigate(
                    EnrollmentEdit(
                        enrollment.uid,
                        route.role,
                        route.clazzGuid,
                        route.personGuid
                    )
                )
            )
        }
    }

    fun onDeleteEnrollment(enrollmentId: String) {
    }

}
