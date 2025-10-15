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
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.apps_detail
import world.respect.shared.navigation.AppsDetail
import world.respect.shared.navigation.LearningUnitDetail
import world.respect.shared.navigation.LearningUnitList
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.RespectAppDataSource
import world.respect.datalayer.compatibleapps.model.RespectAppManifest
import world.respect.lib.opds.model.OpdsGroup
import world.respect.lib.opds.model.OpdsPublication
import world.respect.lib.opds.model.ReadiumLink
import world.respect.datalayer.ext.dataOrNull
import world.respect.libutil.ext.resolve
import world.respect.shared.navigation.NavCommand
import world.respect.shared.util.ext.asUiText

data class AppsDetailUiState(
    val appDetail: DataLoadState<RespectAppManifest>? = null,
    val publications: List<OpdsPublication> = emptyList(),
    val navigation: List<ReadiumLink> = emptyList(),
    val group: List<OpdsGroup> = emptyList(),
    val appIcon: String? = null,
    val isAdded: Boolean = false,
)

class AppsDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val appDataSource: RespectAppDataSource,
) : RespectViewModel(savedStateHandle) {

    private val _uiState = MutableStateFlow(AppsDetailUiState())

    val uiState = _uiState.asStateFlow()

    private val route: AppsDetail = savedStateHandle.toRoute()

    init {
        _appUiState.update {
            it.copy(
                title = Res.string.apps_detail.asUiText()
            )
        }

        viewModelScope.launch {
            appDataSource.compatibleAppsDataSource.getAppAsFlow(
                manifestUrl = route.manifestUrl,
                loadParams = DataLoadParams()
            ).collectLatest { result ->
                if (result is DataReadyState) {
                    _uiState.update {
                        it.copy(
                            appDetail = result,
                            appIcon = route.manifestUrl.resolve(
                                result.data.icon.toString()
                            ).toString()
                        )

                    }
                }

                result.dataOrNull()?.learningUnits?.also { learningUnitsUri ->
                    appDataSource.opdsDataSource.loadOpdsFeed(
                        url = route.manifestUrl.resolve(
                            learningUnitsUri.toString()
                        ),
                        params = DataLoadParams()
                    ).collect { result ->
                        when (result) {
                            is DataReadyState -> {
                                _uiState.update {
                                    it.copy(
                                        publications = result.data.publications ?: emptyList(),
                                        navigation = result.data.navigation ?: emptyList(),
                                        group = result.data.groups ?: emptyList()
                                    )
                                }
                            }

                            else -> {}
                        }
                    }
                }
            }
        }

        viewModelScope.launch {
            appDataSource.compatibleAppsDataSource.appIsAddedToLaunchpadAsFlow(
                manifestUrl = route.manifestUrl
            ).collect { isAdded ->
                _uiState.update {
                    it.copy(isAdded = isAdded)
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
        viewModelScope.launch {
            appDataSource.compatibleAppsDataSource.addAppToLaunchpad(route.manifestUrl)
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
