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
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.datalayer.SchoolDataSource
import world.respect.shared.domain.account.RespectAccountManager
import kotlin.getValue

data class BookmarkListUiState(
    val bookmarks: List<Bookmark> = emptyList(),
    val isLoading: Boolean = true,

    )

class BookmarkListViewModel(
    savedStateHandle: SavedStateHandle,
    private val accountManager: RespectAccountManager,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {
    private val _uiState = MutableStateFlow(BookmarkListUiState())

    val uiState = _uiState.asStateFlow()
    override val scope: Scope = accountManager.requireActiveAccountScope()

    private val schoolDataSource: SchoolDataSource by inject()

    init {
        viewModelScope.launch {
            schoolDataSource.bookmarkDataSource
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
            schoolDataSource.bookmarkDataSource.removeBookmark(bookmark.url)
        }
    }

    fun onClickBookmark(bookmark: Bookmark) {

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