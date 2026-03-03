package world.respect.shared.viewmodel.bookmark

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import world.respect.datalayer.RespectAppDataSource
import world.respect.lib.opds.model.Bookmark
import world.respect.shared.navigation.LearningUnitDetail
import world.respect.shared.navigation.NavCommand
import world.respect.shared.viewmodel.RespectViewModel
import io.ktor.http.Url

data class BookmarkListUiState(
    val bookmarks: List<Bookmark> = emptyList(),
    val isLoading: Boolean = true,

)
class BookmarkListViewModel (
    savedStateHandle: SavedStateHandle,
    private val appDataSource: RespectAppDataSource
) : RespectViewModel(savedStateHandle) {
    private val _uiState = MutableStateFlow(BookmarkListUiState())

    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            appDataSource.bookmarkDataSource
                .getAllBookmarks()
                .collect { bookmarks ->
                    _uiState.update {
                        it.copy(
                            bookmarks = bookmarks,
                            isLoading = false
                        )
                    }
                }
        }
    }

    fun onClickRemoveBookmark(bookmark: Bookmark) {
        viewModelScope.launch {
            appDataSource.bookmarkDataSource.removeBookmark(bookmark.url.toString())
        }
    }
    fun onClickBookmark(bookmark: Bookmark){

        _navCommandFlow.tryEmit(
            value = NavCommand.Navigate(
                LearningUnitDetail.create(
                    learningUnitManifestUrl = Url(bookmark.learningUnitUrl),
                    appManifestUrl = Url(bookmark.appManifestUrl),
                    refererUrl = Url(
                        bookmark.refererUrl.toString()
                    ),
                    expectedIdentifier = bookmark.expectedIdentifier
                )
            )
        )
    }
}