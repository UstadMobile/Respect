package world.respect.app.viewmodel.apps.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import respect.composeapp.generated.resources.Res
import respect.composeapp.generated.resources.apps_detail
import world.respect.app.app.AppsDetail
import world.respect.app.app.LessonDetail
import world.respect.app.app.LessonList
import world.respect.app.datasource.RespectAppDataSourceProvider
import world.respect.app.viewmodel.RespectViewModel
import world.respect.datasource.DataLoadParams
import world.respect.datasource.DataLoadResult
import world.respect.datasource.compatibleapps.model.RespectAppManifest
import world.respect.datasource.opds.model.OpdsPublication
import world.respect.navigation.NavCommand

data class AppsDetailUiState(
    val appDetail: RespectAppManifest? = null,
    val publications: List<OpdsPublication> = emptyList(),
)

class AppsDetailViewModel(
    savedStateHandle: SavedStateHandle,
    dataSourceProvider: RespectAppDataSourceProvider,
) : RespectViewModel(savedStateHandle) {


    private val _uiState = MutableStateFlow(AppsDetailUiState())

    val uiState = _uiState.asStateFlow()

    private val route: AppsDetail = savedStateHandle.toRoute()

    private val dataSource = dataSourceProvider.getDataSource(activeAccount)

    init {
        //Get the argument here
        println(route.manifestUrl)
        val manifestUrl = route.manifestUrl

        viewModelScope.launch {
            _appUiState.update {
                it.copy(title = getString(resource = Res.string.apps_detail))
            }
            dataSource.compatibleAppsDataSource.getApp(
                manifestUrl = manifestUrl,
                loadParams = DataLoadParams()
            ).collect { result ->
                when (result) {
                    is DataLoadResult -> {
                        val appDetail = result.data
                        _uiState.update {
                            it.copy(
                                appDetail = appDetail
                            )
                        }
                    }

                    else -> {}
                }
            }
            //once navigation is fixed will pass argument learning units
            dataSource.opdsDataSource.loadOpdsFeed(
                url = manifestUrl,
                params = DataLoadParams()
            ).collect { result ->
                when (result) {
                    is DataLoadResult -> {
                        _uiState.update {
                            it.copy(
                                publications = result.data?.publications ?: emptyList(),
                            )
                        }
                    }

                    else -> {}
                }
            }

        }
    }

    fun onClickLessonList() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                LessonList(manifestUrl = route.manifestUrl)
            )
        )
    }

    fun onClickLesson() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                LessonDetail(manifestUrl = route.manifestUrl)
            )
        )
    }
}
