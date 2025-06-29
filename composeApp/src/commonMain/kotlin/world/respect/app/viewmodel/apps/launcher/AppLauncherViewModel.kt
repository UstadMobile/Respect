package world.respect.app.viewmodel.apps.launcher

import androidx.lifecycle.SavedStateHandle
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
import world.respect.app.app.AppsDetail
import world.respect.app.appstate.FabUiState
import world.respect.app.model.applist.FakeAppDataSource
import world.respect.app.viewmodel.RespectViewModel
import world.respect.datasource.DataLoadParams
import world.respect.datasource.DataLoadResult
import world.respect.datasource.compatibleapps.model.RespectAppManifest
import world.respect.navigation.NavCommand

data class AppLauncherUiState(
    val appList: List<RespectAppManifest> = emptyList(),
)

class AppLauncherViewModel(
    savedStateHandle: SavedStateHandle,
) : RespectViewModel(savedStateHandle) {

    private val appDataSource: FakeAppDataSource = FakeAppDataSource()

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
                            _navCommandFlow.tryEmit(NavCommand.Navigate(AppList))
                        }
                    )
                )
            }

            appDataSource.getAddableApps(
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

    fun onClickApp(app: RespectAppManifest){
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                //Placeholder string
                AppsDetail(manifestUrl = app.license)
            )
        )
    }
}

