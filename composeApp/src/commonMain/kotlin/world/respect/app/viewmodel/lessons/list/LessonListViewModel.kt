package world.respect.app.viewmodel.lessons.list

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import respect.composeapp.generated.resources.Res
import respect.composeapp.generated.resources.lesson_list
import world.respect.app.appstate.AppBarSearchUiState
import world.respect.app.model.lesson.FakeOpdsDataSource
import world.respect.app.viewmodel.RespectViewModel
import world.respect.datasource.DataLoadParams
import world.respect.datasource.DataLoadResult
import world.respect.datasource.opds.model.OpdsFacet
import world.respect.datasource.opds.model.OpdsPublication

data class LessonListUiState(
    val publications: List<OpdsPublication> = emptyList(),
    val lessonFilter: List<OpdsFacet> = emptyList(),
    val selectedFilterTitle: String? = null
)

class LessonListViewModel(private val opdsDataSource: FakeOpdsDataSource = FakeOpdsDataSource()) : RespectViewModel() {
    private val _uiState = MutableStateFlow(LessonListUiState())
    val uiState = _uiState.asStateFlow()

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
        }
        loadLessonListData()
    }

    private fun loadLessonListData() {
        viewModelScope.launch {
            opdsDataSource.loadOpdsFeed(
                url = "https://your.api.endpoint/opds/lessons",
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

    fun onFilterSelected(title: String) {
        _uiState.update { it.copy(selectedFilterTitle = title) }
    }
}
