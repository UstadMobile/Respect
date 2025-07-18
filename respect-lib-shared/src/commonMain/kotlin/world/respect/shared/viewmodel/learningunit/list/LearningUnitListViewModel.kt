package world.respect.shared.viewmodel.learningunit.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import io.ktor.http.Url
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import world.respect.datalayer.DataErrorResult
import world.respect.shared.navigation.LearningUnitDetail
import world.respect.shared.navigation.LearningUnitList
import world.respect.shared.viewmodel.app.appstate.AppBarSearchUiState
import world.respect.shared.datasource.RespectAppDataSourceProvider
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadingState
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.opds.model.OpdsFacet
import world.respect.datalayer.opds.model.OpdsGroup
import world.respect.datalayer.opds.model.OpdsPublication
import world.respect.datalayer.opds.model.ReadiumLink
import world.respect.libutil.ext.resolve
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.something_went_wrong
import world.respect.shared.navigation.NavCommand

data class LearningUnitListUiState(
    val publications: List<OpdsPublication> = emptyList(),
    val navigation: List<ReadiumLink> = emptyList(),
    val group: List<OpdsGroup> = emptyList(),
    val lessonFilter: List<OpdsFacet> = emptyList(),
    val selectedFilterTitle: String? = null,
    val isLoading: Boolean = true,
    val snackBarMessage: String? = null
)

class LearningUnitListViewModel(
    savedStateHandle: SavedStateHandle,
    dataSourceProvider: RespectAppDataSourceProvider
) : RespectViewModel(savedStateHandle) {

    private val _uiState = MutableStateFlow(LearningUnitListUiState())

    val uiState = _uiState.asStateFlow()

    private val dataSource = dataSourceProvider.getDataSource(activeAccount)

    private val route: LearningUnitList = savedStateHandle.toRoute()


    init {
        viewModelScope.launch {
            _appUiState.update {
                it.copy(
                    searchState = AppBarSearchUiState(
                        visible = true
                    )
                )
            }

            dataSource.opdsDataSource.loadOpdsFeed(
                url = route.opdsFeedUrl,
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

                        val appBarTitle = result.data.metadata.title

                        _appUiState.update {
                            it.copy(
                                title = appBarTitle,
                                searchState = AppBarSearchUiState(visible = true)
                            )
                        }

                        _uiState.update {
                            it.copy(
                                navigation = result.data.navigation ?: emptyList(),
                                publications = result.data.publications ?: emptyList(),
                                group = result.data.groups ?: emptyList(),
                                lessonFilter = result.data.facets ?: emptyList(),
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
            }
        }
    }

    fun onClickFilter(title: String) {
        _uiState.update { it.copy(selectedFilterTitle = title) }
    }

    fun onClickPublication(publication: OpdsPublication) {

        val publicationHref = publication.links.find {
            it.rel?.contains(SELF) == true
        }?.href.toString()

        val refererUrl = route.opdsFeedUrl.resolve(publicationHref).toString()

        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                LearningUnitDetail.create(
                    learningUnitManifestUrl = route.opdsFeedUrl.resolve(
                        publicationHref
                    ),
                    appManifestUrl = route.appManifestUrl,
                    refererUrl = Url(
                        refererUrl
                    ),
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
                    opdsFeedUrl = route.opdsFeedUrl.resolve(
                        navigationHref
                    ),
                    appManifestUrl = route.appManifestUrl,
                )
            )
        )
    }

    fun onClearSnackBar() {
        _uiState.update {
            it.copy(
                snackBarMessage = null
            )
        }
    }

    companion object {
        const val SELF = "self"
        const val ICON = "icon"


    }
}
