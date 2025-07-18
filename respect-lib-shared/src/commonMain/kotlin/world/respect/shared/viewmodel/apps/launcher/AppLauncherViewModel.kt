package world.respect.shared.viewmodel.apps.launcher

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.app
import world.respect.shared.generated.resources.apps
import world.respect.shared.generated.resources.invalid_url
import world.respect.shared.navigation.RespectAppList
import world.respect.shared.navigation.AppsDetail
import world.respect.shared.viewmodel.app.appstate.FabUiState
import world.respect.shared.datasource.RespectAppDataSourceProvider
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.compatibleapps.model.RespectAppManifest
import world.respect.shared.navigation.NavCommand

data class AppLauncherUiState(
    val appList: List<DataLoadState<RespectAppManifest>> = emptyList(),
    val snackBarMessage: String? = null,
)

class AppLauncherViewModel(
    savedStateHandle: SavedStateHandle,
    dataSourceProvider: RespectAppDataSourceProvider,
) : RespectViewModel(savedStateHandle) {

    private val _uiState = MutableStateFlow(AppLauncherUiState())

    val uiState = _uiState.asStateFlow()

    private val dataSource = dataSourceProvider.getDataSource(activeAccount)

    var errorMessage: String = ""

    init {
        viewModelScope.launch {
            errorMessage = getString(resource = Res.string.invalid_url)

            _appUiState.update {
                it.copy(
                    title = getString(resource = Res.string.apps),
                    showBackButton = false,
                    fabState = FabUiState(
                        visible = true,
                        icon = FabUiState.FabIcon.ADD,
                        text = getString(resource = Res.string.app),
                        onClick = {
                            _navCommandFlow.tryEmit(
                                NavCommand.Navigate(
                                    RespectAppList
                                )
                            )
                        }
                    )
                )
            }

            dataSource.compatibleAppsDataSource.getLaunchpadApps(
                loadParams = DataLoadParams()
            ).collect { result ->
                when (result) {
                    is DataReadyState -> {
                        val appList = result.data
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

    fun onClickApp(app: DataLoadState<RespectAppManifest>) {
        val url = app.metaInfo.url ?: return

        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                AppsDetail.create(url)
            )
        )
    }

    fun onClickRemove(app: RespectAppManifest) {
        _uiState.update { state ->
            state.copy(
                appList = state.appList.filterNot { it == app }
            )
        }
    }

    fun onClearSnackBar() {
        _uiState.update {
            it.copy(
                snackBarMessage = null
            )
        }
    }
}

