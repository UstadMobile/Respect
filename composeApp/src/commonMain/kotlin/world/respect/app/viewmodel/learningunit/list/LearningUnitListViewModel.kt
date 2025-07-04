package world.respect.app.viewmodel.learningunit.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import respect.composeapp.generated.resources.Res
import respect.composeapp.generated.resources.lesson_list
import world.respect.app.app.LearningUnitDetail
import world.respect.app.app.LearningUnitList
import world.respect.app.appstate.AppBarSearchUiState
import world.respect.app.datasource.RespectAppDataSourceProvider
import world.respect.app.viewmodel.RespectViewModel
import world.respect.datasource.DataLoadParams
import world.respect.datasource.DataLoadResult
import world.respect.datasource.opds.model.OpdsFacet
import world.respect.datasource.opds.model.OpdsPublication
import world.respect.datasource.opds.model.ReadiumLink
import world.respect.navigation.NavCommand

data class LearningUnitListUiState(
    val publications: List<OpdsPublication> = emptyList(),
    val lessonFilter: List<OpdsFacet> = emptyList(),
    val selectedFilterTitle: String? = null,
    val link: List<ReadiumLink> = emptyList(),
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
                    title = getString(resource = Res.string.lesson_list),
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
                    is DataLoadResult -> {
                        _uiState.update {
                            it.copy(
                                publications = result.data?.publications ?: emptyList(),
                                lessonFilter = result.data?.facets ?: emptyList(),
                                link = result.data?.links ?: emptyList()

                            )
                        }
                    }

                    else -> {}
                }
            }
        }
    }

    fun onClickFilter(title: String) {
        _uiState.update { it.copy(selectedFilterTitle = title) }
    }

    fun onClickLesson(publication: OpdsPublication) {
        val publicationSelfLink = publication.links.find { it.rel?.equals("self") == true }?.href

        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                LearningUnitDetail(
                    learningUnitManifestUrl = publicationSelfLink?:"",
                    refererUrl = route.opdsFeedUrl,
                    expectedIdentifier = publication.metadata.identifier.toString()
                )
            )
        )
    }
}
