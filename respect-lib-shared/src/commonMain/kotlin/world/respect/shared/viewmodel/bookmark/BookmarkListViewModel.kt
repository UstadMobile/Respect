package world.respect.shared.viewmodel.bookmark

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import world.respect.datalayer.school.model.Bookmark
import world.respect.shared.viewmodel.RespectViewModel
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.ext.dataOrNull
import world.respect.datalayer.school.BookmarkDataSource
import world.respect.datalayer.school.model.StatusEnum
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.viewmodel.app.appstate.LoadingUiState
import kotlin.getValue

data class BookmarkListUiState(
    val bookmarks: List<Bookmark> = emptyList()
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
            loadingState = LoadingUiState.INDETERMINATE

            val personUid = accountManager.activeAccount?.userGuid ?: return@launch

            val bookmarks = schoolDataSource.bookmarkDataSource.list(
                    loadParams = DataLoadParams(),
                    listParams = BookmarkDataSource.GetListParams(
                        personUid = personUid
                    )
                ).dataOrNull() ?: emptyList()

            _uiState.update {
                it.copy(
                    bookmarks = bookmarks,
                )
            }

            loadingState = LoadingUiState.NOT_LOADING
        }
    }

    fun onClickRemoveBookmark(bookmark: Bookmark) {
        viewModelScope.launch {
            loadingState = LoadingUiState.INDETERMINATE

            val updatedBookmark = bookmark.copy(
                status = StatusEnum.TO_BE_DELETED
            )

            schoolDataSource.bookmarkDataSource.store(
                listOf(updatedBookmark)
            )
            loadingState = LoadingUiState.NOT_LOADING
        }
    }

    fun onClickBookmark(bookmark: Bookmark){
        //Have to implement navigation
    }
}