package world.respect.app.viewmodel.apps.list


import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import world.respect.app.model.applist.AppListModel
import world.respect.app.viewmodel.RespectViewModel
import respect.composeapp.generated.resources.Res
import respect.composeapp.generated.resources.select_app
import world.respect.app.model.applist.FakeAppDataSource
import world.respect.datasource.DataLoadParams
import world.respect.datasource.DataLoadResult
import world.respect.datasource.compatibleapps.model.RespectAppManifest


data class AppListUiState(
    val appList: List<RespectAppManifest> = emptyList()
)

class AppListViewModel(
    private val appDataSource: FakeAppDataSource = FakeAppDataSource()
) : RespectViewModel() {

    private val _uiState = MutableStateFlow(AppListUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _appUiState.update {
                it.copy(
                    title = getString(resource = Res.string.select_app),
                )
            }
        }
        loadAppList()
    }

    private fun loadAppList() {
        viewModelScope.launch {
            appDataSource.getLaunchpadApps(
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
                    else -> {
                    }
                }
            }
        }
    }
}
