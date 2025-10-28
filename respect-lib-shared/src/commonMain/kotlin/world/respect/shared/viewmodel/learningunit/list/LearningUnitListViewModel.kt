package world.respect.shared.viewmodel.learningunit.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import io.ktor.http.Url
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import world.respect.shared.navigation.LearningUnitDetail
import world.respect.shared.navigation.LearningUnitList
import world.respect.shared.viewmodel.app.appstate.AppBarSearchUiState
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.RespectAppDataSource
import world.respect.lib.opds.model.OpdsFacet
import world.respect.lib.opds.model.OpdsGroup
import world.respect.lib.opds.model.OpdsPublication
import world.respect.lib.opds.model.ReadiumLink
import world.respect.libutil.ext.resolve
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.language
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.NavResultReturner
import world.respect.shared.navigation.sendResultIfResultExpected
import world.respect.shared.util.SortOrderOption
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.learningunit.LearningUnitSelection

data class LearningUnitListUiState(
    val publications: List<OpdsPublication> = emptyList(),
    val navigation: List<ReadiumLink> = emptyList(),
    val group: List<OpdsGroup> = emptyList(),
    val facetOptions: List<OpdsFacet> = emptyList(),
    val selectedFilterTitle: String? = null,
    val sortOptions: List<SortOrderOption> = emptyList(),
    val activeSortOrderOption: SortOrderOption = SortOrderOption(
        Res.string.language, 1, true
    ),
    val fieldsEnabled: Boolean = true
)

class LearningUnitListViewModel(
    savedStateHandle: SavedStateHandle,
    private val appDataSource: RespectAppDataSource,
    private val resultReturner: NavResultReturner,
) : RespectViewModel(savedStateHandle) {

    private val _uiState = MutableStateFlow(LearningUnitListUiState())

    val uiState = _uiState.asStateFlow()

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

            appDataSource.opdsDataSource.loadOpdsFeed(
                url = route.opdsFeedUrl,
                params = DataLoadParams()
            ).collect { result ->
                when (result) {
                    is DataReadyState -> {

                        val appBarTitle = result.data.metadata.title
                        val facetOptions = result.data.facets ?: emptyList()
                        val sortOptions = facetOptions.mapIndexed { index, facet ->
                            SortOrderOption(
                                fieldMessageId = Res.string.language,
                                flag = index + 1,
                                order = true
                            )
                        }

                        _appUiState.update {
                            it.copy(
                                title = appBarTitle.asUiText(),
                                searchState = AppBarSearchUiState(visible = true)
                            )
                        }

                        _uiState.update {
                            it.copy(
                                navigation = result.data.navigation ?: emptyList(),
                                publications = result.data.publications ?: emptyList(),
                                group = result.data.groups ?: emptyList(),
                                facetOptions = facetOptions,
                                sortOptions = sortOptions
                            )
                        }
                    }

                    else -> {}
                }
            }
        }
    }

    fun onSortOrderChanged(sortOption: SortOrderOption) {
        _uiState.update {
            it.copy(activeSortOrderOption = sortOption)
        }
    }

    fun onClickPublication(publication: OpdsPublication) {
        val publicationHref = publication.links.find {
            it.rel?.contains(SELF) == true
        }?.href.toString()

        val refererUrl = route.opdsFeedUrl.resolve(publicationHref).toString()
        val learningUnitManifestUrl = route.opdsFeedUrl.resolve(publicationHref)

        if(
            !resultReturner.sendResultIfResultExpected(
                route = route,
                navCommandFlow = _navCommandFlow,
                result = LearningUnitSelection(
                    learningUnitManifestUrl = learningUnitManifestUrl,
                    selectedPublication = publication,
                    appManifestUrl = route.appManifestUrl,
                )
            )
        ) {
            _navCommandFlow.tryEmit(
                value = NavCommand.Navigate(
                    LearningUnitDetail.create(
                        learningUnitManifestUrl = learningUnitManifestUrl,
                        appManifestUrl = route.appManifestUrl,
                        refererUrl = Url(
                            refererUrl
                        ),
                        expectedIdentifier = publication.metadata.identifier.toString()
                    )
                )
            )
        }
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
                    resultPopUpTo = route.resultPopUpTo,
                    resultKey = route.resultKey,
                )
            )
        )
    }

    companion object {
        const val SELF = "self"
        const val ICON = "icon"


    }
}
