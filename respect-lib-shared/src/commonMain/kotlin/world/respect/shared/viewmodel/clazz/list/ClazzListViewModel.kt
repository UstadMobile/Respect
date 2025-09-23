package world.respect.shared.viewmodel.clazz.list

import androidx.lifecycle.SavedStateHandle
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
import world.respect.shared.generated.resources.classes
import world.respect.shared.generated.resources.clazz
import world.respect.shared.generated.resources.first_name
import world.respect.shared.generated.resources.last_name
import world.respect.shared.navigation.ClazzDetail
import world.respect.shared.navigation.ClazzEdit
import world.respect.shared.navigation.NavCommand
import world.respect.shared.util.SortOrderOption
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.FabUiState

data class ClazzListUiState(
    val classes: IPagingSourceFactory<Int, Clazz> = EmptyPagingSourceFactory(),
    val sortOptions: List<SortOrderOption> = emptyList(),
    val activeSortOrderOption: SortOrderOption = SortOrderOption(
        Res.string.first_name, 1, true
    ),
    val fieldsEnabled: Boolean = true
)

class ClazzListViewModel(
    savedStateHandle: SavedStateHandle,
    accountManager: RespectAccountManager,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireSelectedAccountScope()

    private val schoolDataSource: SchoolDataSource by inject()

    private val _uiState = MutableStateFlow(ClazzListUiState())

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
                title = Res.string.classes.asUiText(),
                fabState = FabUiState(
                    visible = true,
                    icon = FabUiState.FabIcon.ADD,
                    text = Res.string.clazz.asUiText(),
                    onClick = ::onClickAdd
                )
            )
        }

        _uiState.update { prev ->
            prev.copy(
                classes = pagingSourceHolder,
                sortOptions = listOf(
                    SortOrderOption(
                        Res.string.first_name,
                        flag = 1,
                        order = true
                    ),
                    SortOrderOption(
                        Res.string.last_name,
                        flag = 2,
                        order = true
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

    fun onClickClazz(clazz: Clazz) {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                ClazzDetail(clazz.guid)
            )
        )
    }

    fun onClickAdd() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(ClazzEdit(guid = null))
        )
    }
}

