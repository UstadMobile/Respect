package world.respect.shared.viewmodel.apps.list


import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.select_app
import world.respect.shared.navigation.AppsDetail
import world.respect.shared.navigation.EnterLink
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.RespectAppDataSource
import world.respect.datalayer.compatibleapps.model.RespectAppManifest
import world.respect.shared.navigation.NavCommand
import world.respect.shared.util.ext.asUiText



data class AppListUiState(
    val appList: List<DataLoadState<RespectAppManifest>> = emptyList()
)

class AppListViewModel(
    savedStateHandle: SavedStateHandle,
    private val appDataSource: RespectAppDataSource,
) : RespectViewModel(savedStateHandle) {

    private val _uiState = MutableStateFlow(AppListUiState())

    val uiState = _uiState.asStateFlow()

    init {
        _appUiState.update {
            it.copy(
                title = Res.string.select_app.asUiText(),
            )
        }

        viewModelScope.launch {
            appDataSource.compatibleAppsDataSource.getAddableApps(
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
                EnterLink.create()
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

