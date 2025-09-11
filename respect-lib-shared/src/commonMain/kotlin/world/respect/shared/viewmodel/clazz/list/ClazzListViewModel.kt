package world.respect.shared.viewmodel.clazz.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.DataLoadingState
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.oneroster.composites.ClazzListDetails
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.classes
import world.respect.shared.generated.resources.clazz
import world.respect.shared.generated.resources.first_name
import world.respect.shared.generated.resources.last_name
import world.respect.shared.navigation.ClazzEdit
import world.respect.shared.navigation.ClazzDetail
import world.respect.shared.navigation.NavCommand
import world.respect.shared.util.SortOrderOption
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.FabUiState
import kotlin.getValue

data class ClazzListUiState(
    val clazz: DataLoadState<List<ClazzListDetails>> = DataLoadingState(),
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

        viewModelScope.launch {
            schoolDataSource.onRoasterDataSource.findAll(DataLoadParams())
                .collect { dataState ->
                    _uiState.update { state ->
                        val sortOptions = listOf(
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

                        state.copy(
                            clazz = dataState,
                            sortOptions = sortOptions,
                            activeSortOrderOption = sortOptions.first()
                        )
                    }
                }
        }

    }

    fun onSortOrderChanged(sortOption: SortOrderOption) {
        _uiState.update {
            it.copy(activeSortOrderOption = sortOption)
        }
    }

    fun onClickClazz(sourcedId: String) {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                ClazzDetail.create(sourcedId)
            )
        )
    }

    fun onClickAdd() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(ClazzEdit(sourcedId = null))
        )
    }
}

