package world.respect.shared.viewmodel.enrollment.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.getString
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.ext.dataOrNull
import world.respect.datalayer.school.EnrollmentDataSource
import world.respect.datalayer.school.model.Enrollment
import world.respect.datalayer.school.model.EnrollmentRoleEnum
import world.respect.datalayer.shared.paging.EmptyPagingSourceFactory
import world.respect.datalayer.shared.paging.IPagingSourceFactory
import world.respect.datalayer.shared.paging.PagingSourceFactoryHolder
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.ext.toLocalizedDate
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.enrollment_for
import world.respect.shared.navigation.EnrollmentEdit
import world.respect.shared.navigation.EnrollmentList
import world.respect.shared.navigation.NavCommand
import world.respect.shared.util.ext.asUiText
import world.respect.shared.util.ext.fullName
import world.respect.shared.viewmodel.RespectViewModel

data class EnrollmentListUiState(
    val enrollments: IPagingSourceFactory<Int, Enrollment> = EmptyPagingSourceFactory(),
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

        viewModelScope.launch {
            val personSelected = schoolDataSource.personDataSource.findByGuid(
                loadParams = DataLoadParams(),
                guid = route.personGuid,
            ).dataOrNull()

            val clazzSelected = schoolDataSource.classDataSource.findByGuid(
                params = DataLoadParams(),
                guid = route.clazzGuid,
            ).dataOrNull()

            val personName = personSelected?.fullName() ?: ""
            val clazzName = clazzSelected?.title ?: ""
            val titleString: String = getString(Res.string.enrollment_for, personName, clazzName)
            _appUiState.update {
                it.copy(
                    title = titleString.asUiText()
                )
            }
        }
        _uiState.update {
            it.copy(enrollments = pagingSourceHolder)
        }
    }

    fun onEditEnrollment(enrollment: Enrollment?) {
        if (enrollment != null) {
            _navCommandFlow.tryEmit(
                NavCommand.Navigate(
                    EnrollmentEdit(
                        enrollment.uid,
                        route.role,
                        route.personGuid,
                        route.clazzGuid
                    )
                )
            )
        }
    }

    fun onDeleteEnrollment(enrollmentId: String) {
        viewModelScope.launch {
            schoolDataSource.enrollmentDataSource.deleteEnrollment(
                uid = enrollmentId
            )
            pagingSourceHolder.invalidate()
        }
    }

    fun onDateFormatted(date: LocalDate?): String {
        return date?.toString()?.toLocalizedDate(outputPattern = "dd/MM/yyyy") ?: ""
    }
}
