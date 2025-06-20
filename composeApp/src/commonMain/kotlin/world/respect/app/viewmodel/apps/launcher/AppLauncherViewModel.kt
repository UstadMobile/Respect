package world.respect.app.viewmodel.apps.launcher

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import respect.composeapp.generated.resources.Res
import respect.composeapp.generated.resources.app
import respect.composeapp.generated.resources.apps
import world.respect.app.app.AppList
import world.respect.app.appstate.FabUiState
import world.respect.app.model.applauncher.AppLauncherModel
import world.respect.app.model.applist.FakeAppDataSource
import world.respect.app.viewmodel.RespectViewModel
import world.respect.datasource.DataLoadParams
import world.respect.datasource.DataLoadResult
import world.respect.datasource.compatibleapps.model.RespectAppManifest

data class AppLauncherUiState(
    val appList: List<RespectAppManifest> = emptyList(),
)

class AppLauncherViewModel(private val appDataSource: FakeAppDataSource = FakeAppDataSource()) : RespectViewModel() {

    private val _uiState = MutableStateFlow(AppLauncherUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _appUiState.update {
                it.copy(
                    title = getString(resource = Res.string.apps),
                    showBackButton = false,
                    fabState = FabUiState(
                        visible = true,
                        icon = FabUiState.FabIcon.ADD,
                        text = getString(resource = Res.string.app),
                        onClick = {
                            navController.navigate(AppList)
                        })
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

