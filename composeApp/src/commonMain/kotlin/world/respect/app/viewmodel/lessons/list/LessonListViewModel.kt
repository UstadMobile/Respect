package world.respect.app.viewmodel.lessons.list

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
import world.respect.app.app.LessonDetail
import world.respect.app.app.LessonList
import world.respect.app.appstate.AppBarSearchUiState
import world.respect.app.datasource.fakeds.FakeOpdsDataSource
import world.respect.app.viewmodel.RespectViewModel
import world.respect.datasource.DataLoadParams
import world.respect.datasource.DataLoadResult
import world.respect.datasource.opds.model.OpdsFacet
import world.respect.datasource.opds.model.OpdsPublication
import world.respect.navigation.NavCommand

data class LessonListUiState(
    val publications: List<OpdsPublication> = emptyList(),
    val lessonFilter: List<OpdsFacet> = emptyList(),
    val selectedFilterTitle: String? = null
)

class LessonListViewModel(
    savedStateHandle: SavedStateHandle
) : RespectViewModel(savedStateHandle) {

    private val opdsDataSource: FakeOpdsDataSource = FakeOpdsDataSource()

    private val _uiState = MutableStateFlow(LessonListUiState())

    val uiState = _uiState.asStateFlow()

    private val route: LessonList = savedStateHandle.toRoute()

    init {

        val manifestUrl = route.manifestUrl

        viewModelScope.launch {
            _appUiState.update {
                it.copy(
                    title = getString(resource = Res.string.lesson_list),
                    searchState = AppBarSearchUiState(
                        visible = true
                    )
                )
            }
            opdsDataSource.loadOpdsFeed(
                url = manifestUrl,
                params = DataLoadParams()
            ).collect { result ->
                when (result) {
                    is DataLoadResult -> {
                        _uiState.update {
                            it.copy(
                                publications = result.data?.publications ?: emptyList(),
                                lessonFilter = result.data?.facets ?: emptyList()
                            )
                        }
                    }

                    else -> {

                    }
                }
            }
        }
    }

    fun onClickFilter(title: String) {
        _uiState.update { it.copy(selectedFilterTitle = title) }
    }
    fun onClickLesson() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                LessonDetail(manifestUrl = route.manifestUrl)
            )
        )
    }
}
