package world.respect.shared.viewmodel.apps.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import io.ktor.http.Url
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import world.respect.datalayer.DataErrorResult
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.apps_detail
import world.respect.shared.navigation.AppsDetail
import world.respect.shared.navigation.LearningUnitDetail
import world.respect.shared.navigation.LearningUnitList
import world.respect.shared.datasource.RespectAppDataSourceProvider
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.DataLoadingState
import world.respect.datalayer.DataReadyState
import world.respect.shared.generated.resources.something_went_wrong
import world.respect.datalayer.compatibleapps.model.RespectAppManifest
import world.respect.datalayer.opds.model.OpdsGroup
import world.respect.datalayer.opds.model.OpdsPublication
import world.respect.datalayer.opds.model.ReadiumLink
import world.respect.datalayer.ext.dataOrNull
import world.respect.libutil.ext.resolve
import world.respect.shared.navigation.NavCommand

data class AppsDetailUiState(
    val appDetail: DataLoadState<RespectAppManifest>? = null,
    val publications: List<OpdsPublication> = emptyList(),
    val navigation: List<ReadiumLink> = emptyList(),
    val group: List<OpdsGroup> = emptyList(),
    val appIcon: String? = null,
    val isLoading: Boolean = true,
    val snackBarMessage: String? = null
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

        viewModelScope.launch {
            _appUiState.update {
                it.copy(
                    title = getString(resource = Res.string.apps_detail)
                )
            }

            dataSource.compatibleAppsDataSource.getAppAsFlow(
                manifestUrl = route.manifestUrl,
                loadParams = DataLoadParams()
            ).collectLatest { result ->
                when (result) {
                    is DataLoadingState -> {
                        _uiState.update {
                            it.copy(
                                isLoading = true
                            )
                        }
                    }

                    is DataReadyState -> {
                        _uiState.update {
                            it.copy(
                                appDetail = result,
                                appIcon = route.manifestUrl.resolve(
                                    result.data.icon.toString()
                                ).toString(),
                                isLoading = false
                            )
                        }
                    }

                    is DataErrorResult -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                snackBarMessage = getString(resource = Res.string.something_went_wrong)
                            )
                        }
                    }

                    else -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false
                            )
                        }
                    }
                }
                result.dataOrNull()?.learningUnits?.also { learningUnitsUri ->
                    dataSource.opdsDataSource.loadOpdsFeed(
                        url = route.manifestUrl.resolve(
                            learningUnitsUri.toString()
                        ),
                        params = DataLoadParams()
                    ).collect { result ->
                        when (result) {
                            is DataLoadingState -> {
                                _uiState.update {
                                    it.copy(
                                        isLoading = true
                                    )
                                }
                            }

                            is DataReadyState -> {
                                _uiState.update {
                                    it.copy(
                                        publications = result.data.publications ?: emptyList(),
                                        navigation = result.data.navigation ?: emptyList(),
                                        group = result.data.groups ?: emptyList(),
                                        isLoading = false
                                    )
                                }
                            }

                            is DataErrorResult -> {
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        snackBarMessage = result.error.message
                                    )
                                }
                            }

                            else -> {
                                _uiState.update {
                                    it.copy(
                                        isLoading = false
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun onClickLessonList() {

        val appManifest = uiState.value.appDetail?.dataOrNull()

        appManifest?.learningUnits?.toString()?.also { uri ->
            _navCommandFlow.tryEmit(
                NavCommand.Navigate(
                    LearningUnitList.create(
                        opdsFeedUrl = route.manifestUrl.resolve(uri),
                        appManifestUrl = route.manifestUrl,
                    )
                )
            )
        }
    }

    fun onClickPublication(publication: OpdsPublication) {

        val publicationHref = publication.links.find {
            it.rel?.equals(SELF) == true
        }?.href.toString()

        val refererUrl = uiState.value.appDetail?.dataOrNull()?.learningUnits.toString()

        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                LearningUnitDetail.create(
                    learningUnitManifestUrl = route.manifestUrl.resolve(publicationHref),
                    appManifestUrl = route.manifestUrl,
                    refererUrl = Url(refererUrl),
                    expectedIdentifier = publication.metadata.identifier.toString()
                )
            )
        )
    }

    fun onClickNavigation(navigation: ReadiumLink) {

        val navigationHref = navigation.href

        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                LearningUnitList.create(
                    opdsFeedUrl = route.manifestUrl.resolve(navigationHref),
                    appManifestUrl = route.manifestUrl,
                )
            )
        )
    }

    fun onClickTry() {
        /*TRY Button Click*/
    }

    fun onClickAdd() {
        /*Add App Button Click*/

    }

    fun onClearSnackBar() {
        _uiState.update {
            it.copy(
                snackBarMessage = null
            )
        }
    }
    companion object {
        const val BUTTONS_ROW = "buttons_row"
        const val LESSON_HEADER = "lesson_header"
        const val SCREENSHOT = "screenshot"
        const val LEARNING_UNIT_LIST = "learning_unit_list"
        const val SELF = "self"
        const val APP_DETAIL = "app_detail"
    }
}
