package world.respect.shared.viewmodel.statement.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.datalayer.SchoolDataSource
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.DataLoadingState
import world.respect.lib.dataloadstate.ext.map
import world.respect.lib.xapi.ext.sortedByTimestampDescending
import world.respect.lib.xapi.model.XapiStatement
import world.respect.lib.xapi.resources.XapiStatementsResource
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.list
import world.respect.shared.navigation.StatementList
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel


data class StatementListUiState(
    val statements: DataLoadState<List<XapiStatement>> = DataLoadingState(),
)

class StatementListViewModel(
    savedStateHandle: SavedStateHandle,
    accountManager: RespectAccountManager,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()

    private val _uiState = MutableStateFlow(StatementListUiState())

    val uiState: StateFlow<StatementListUiState> = _uiState.asStateFlow()

    private val schoolDataSource: SchoolDataSource by inject()

    private val route: StatementList = savedStateHandle.toRoute()


    init {
        _appUiState.update {
            it.copy(
                title = Res.string.list.asUiText(),
                showBackButton = true,
                hideBottomNavigation = true
            )
        }

        viewModelScope.launch {
            schoolDataSource.xapiStatementsResource.getAsFlow(
                listParams = XapiStatementsResource.GetStatementParams(
                    activity = route.activityId,
                    relatedActivities = true
                ),
                dataLoadParams = DataLoadParams()
            ).collectLatest { loadState ->
                _uiState.update {
                    it.copy(
                        statements = loadState.map { result ->
                            result.statements.sortedByTimestampDescending()
                        }
                    )
                }
            }
        }
    }
}
