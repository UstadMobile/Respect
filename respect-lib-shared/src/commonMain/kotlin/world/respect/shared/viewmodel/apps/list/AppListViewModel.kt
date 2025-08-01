package world.respect.shared.viewmodel.apps.list


import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.select_app
import world.respect.shared.navigation.AppsDetail
import world.respect.shared.navigation.EnterLink
import world.respect.shared.datasource.RespectAppDataSourceProvider
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.compatibleapps.model.RespectAppManifest
import world.respect.shared.navigation.NavCommand


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
                    is DataReadyState -> {
                        _uiState.update {
                            it.copy(
                                appList = result.data
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

    fun onClickApp(app: DataLoadState<RespectAppManifest>) {
        val url = app.metaInfo.url ?: return
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                AppsDetail.create(url)
            )
        )
    }

    companion object{
       const val EMPTY_LIST = "empty_list"
    }
}

