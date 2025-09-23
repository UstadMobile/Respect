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
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.RespectAppDataSource
import world.respect.datalayer.compatibleapps.model.RespectAppManifest
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.Settings
import world.respect.shared.util.ext.asUiText

data class AppLauncherUiState(
    val appList: List<DataLoadState<RespectAppManifest>> = emptyList(),
    val snackbarMessage: String? = null,
)

class AppLauncherViewModel(
    savedStateHandle: SavedStateHandle,
    private val appDataSource: RespectAppDataSource,
) : RespectViewModel(savedStateHandle) {

    private val _uiState = MutableStateFlow(AppLauncherUiState())

    val uiState = _uiState.asStateFlow()

    var errorMessage: String = ""

    init {
        _appUiState.update {
            it.copy(
                title = Res.string.apps.asUiText(),
                settingsIconVisible = true,
                onClickSettings = ::onClickSettings,
                fabState = FabUiState(
                    visible = true,
                    icon = FabUiState.FabIcon.ADD,
                    text = Res.string.app.asUiText(),
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

        viewModelScope.launch {
            errorMessage = getString(resource = Res.string.invalid_url)



            appDataSource.compatibleAppsDataSource.getLaunchpadApps(
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
    fun onClickSettings() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(Settings)
        )
    }

    fun onClickRemove(app: DataLoadState<RespectAppManifest>) {
        val manifestUrl = app.metaInfo.url ?: return
        viewModelScope.launch {
            appDataSource.compatibleAppsDataSource.removeAppFromLaunchpad(
                manifestUrl
            )
        }
    }

    fun clearSnackBar() {
        _uiState.update {
            it.copy(snackbarMessage = null)
        }
    }
}

