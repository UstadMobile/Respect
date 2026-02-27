package world.respect.shared.viewmodel.bookmark

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import world.respect.datalayer.RespectAppDataSource
import world.respect.lib.opds.model.Bookmark

import world.respect.shared.viewmodel.RespectViewModel

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
            appDataSource.opdsDataSource
                .getAllBookmarks()
                .collect { bookmarks ->
                    println("Bookmark ${bookmarks}")
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
            appDataSource.opdsDataSource.removeBookmark(bookmark.url)
        }
    }
}