package world.respect.shared.viewmodel.apps.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.select_app
import world.respect.shared.navigation.AppsDetail
import world.respect.shared.navigation.EnterLink
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.DataLoadingState
import world.respect.lib.dataloadstate.ext.map
import world.respect.datalayer.SchoolDataSource
import world.respect.lib.xapi.OpenEelXapiConstants
import world.respect.lib.xapi.ext.objectActivityOrNull
import world.respect.lib.xapi.ext.webPubManifestAsUrlOrNull
import world.respect.lib.xapi.model.XapiStatement
import world.respect.lib.xapi.model.XapiVerb
import world.respect.lib.xapi.resources.XapiStatementsResource
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.navigation.NavCommand
import world.respect.shared.util.ext.asUiText

data class AppListUiState(
    val appList: DataLoadState<List<XapiStatement>> = DataLoadingState()
)

class AppListViewModel(
    savedStateHandle: SavedStateHandle,
    accountManager: RespectAccountManager,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()

    private val _uiState = MutableStateFlow(AppListUiState())

    val uiState = _uiState.asStateFlow()

    private val schoolDataSource: SchoolDataSource by inject()

    init {
        _appUiState.update {
            it.copy(title = Res.string.select_app.asUiText())
        }

        viewModelScope.launch {
            schoolDataSource.xapiStatementsResource.getAsFlow(
                listParams = XapiStatementsResource.GetStatementParams(
                    verb = XapiVerb.ID_LISTED_APP,
                    activity = OpenEelXapiConstants.CATEGORY_APP_LISTING_RECIPE,
                    relatedActivities = true,
                ),
                dataLoadParams = DataLoadParams(),
            ).collectLatest { state ->
                _uiState.update { prev ->
                    prev.copy(appList = state.map { result -> result.statements })
                }
            }
        }
    }

    fun onClickAddLink() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                EnterLink
            )
        )
    }

    fun onClickApp(app: XapiStatement) {
        val manifest = app.objectActivityOrNull()?.definition?.webPubManifestAsUrlOrNull() ?: return
        _navCommandFlow.tryEmit(NavCommand.Navigate(AppsDetail.create(manifest)))
    }
}