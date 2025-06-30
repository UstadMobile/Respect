package world.respect.app.viewmodel.apps.list


import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import world.respect.app.viewmodel.RespectViewModel
import respect.composeapp.generated.resources.Res
import respect.composeapp.generated.resources.select_app
import world.respect.app.app.AppsDetail
import world.respect.app.app.EnterLink
import world.respect.app.datasource.RespectAppDataSourceProvider
import world.respect.datasource.DataLoadParams
import world.respect.datasource.DataLoadResult
import world.respect.datasource.DataLoadState
import world.respect.datasource.compatibleapps.model.RespectAppManifest
import world.respect.navigation.NavCommand


data class AppListUiState(
    val appList: List<DataLoadState<RespectAppManifest>> = emptyList()
)

class AppListViewModel(
    savedStateHandle: SavedStateHandle,
    dataSourceProvider: RespectAppDataSourceProvider,
) : RespectViewModel(savedStateHandle) {


    private val dataSource = dataSourceProvider.getDataSource(activeAccount)

    private val _uiState = MutableStateFlow(AppListUiState())

    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _appUiState.update {
                it.copy(
                    title = getString(resource = Res.string.select_app),
                )
            }

            dataSource.compatibleAppsDataSource.getAddableApps(
                loadParams = DataLoadParams()
            ).collect { result ->
                when (result) {
                    is DataLoadResult -> {
                        val appList = result.data ?: emptyList()
                        _uiState.update {
                            it.copy(
                                appList = appList
                            )
                        }
                    }

                    else -> {}
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

    fun onClickApp(app: RespectAppManifest){
        println("ENTER LINK FUNCTION ")

        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                //Placeholder string
                AppsDetail(manifestUrl = app.license)
            )
        )
    }
}
