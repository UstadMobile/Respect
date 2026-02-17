package world.respect.shared.viewmodel.sharedschooldevice.login

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
import world.respect.datalayer.school.ClassDataSource
import world.respect.datalayer.school.model.Clazz
import world.respect.datalayer.shared.paging.EmptyPagingSourceFactory
import world.respect.datalayer.shared.paging.IPagingSourceFactory
import world.respect.datalayer.shared.paging.PagingSourceFactoryHolder
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.login
import world.respect.shared.generated.resources.select_class
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.ScanQRCode
import world.respect.shared.navigation.SelectClass
import world.respect.shared.navigation.StudentList
import world.respect.shared.navigation.TeacherAndAdminLogin
import world.respect.shared.resources.UiText
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel

data class SelectClassUiState(
    val error: UiText? = null,
    val classes: IPagingSourceFactory<Int, Clazz> = EmptyPagingSourceFactory(),
    val isSelfSelectClassAndName: Boolean = true
)

class SelectClassViewModel(
    savedStateHandle: SavedStateHandle,
    accountManager: RespectAccountManager,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()

    private val route: SelectClass = savedStateHandle.toRoute()

    private val schoolDataSource: SchoolDataSource by inject()

    private val _uiState = MutableStateFlow(SelectClassUiState())
    val uiState = _uiState.asStateFlow()

    private val pagingSourceHolder = PagingSourceFactoryHolder {
        schoolDataSource.classDataSource.listAsPagingSource(
            loadParams = DataLoadParams(),
            params = ClassDataSource.GetListParams()
        )
    }


    init {
        _appUiState.update {
            it.copy(
                title = if (route.isSelfSelectClassAndName) Res.string.select_class.asUiText() else Res.string.login.asUiText(),
                hideBottomNavigation = true,
                userAccountIconVisible = false,
                showBackButton = false
            )
        }
        _uiState.update { prev ->
            prev.copy(
                classes = pagingSourceHolder,
                isSelfSelectClassAndName = route.isSelfSelectClassAndName
            )
        }
    }

    fun onClickScanQrCode() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                ScanQRCode.create()
            )
        )
    }

    fun onClickTeacherAdminLogin() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(TeacherAndAdminLogin)
        )
    }

    fun onClickClazz(clazz: Clazz) {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                StudentList(
                    className = clazz.title,
                    guid = clazz.guid
                )
            )
        )
    }
}