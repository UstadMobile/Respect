package world.respect.shared.viewmodel.bookmark

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
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
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.DataLoadingState
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.ext.dataOrNull
import world.respect.datalayer.school.BookmarkDataSource
import world.respect.datalayer.school.model.BookmarkDetails
import world.respect.datalayer.school.model.StatusEnum
import world.respect.lib.opds.model.OpdsPublication
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.home
import world.respect.shared.navigation.LearningUnitDetail
import world.respect.shared.navigation.NavCommand
import world.respect.shared.util.ext.asUiText
import kotlin.getValue

data class BookmarkListUiState(
    val bookmarkDetails: List<BookmarkDetails> = emptyList(),
    val app: DataLoadState<OpdsPublication> = DataLoadingState(),
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
        _appUiState.update {
            it.copy(title = Res.string.home.asUiText())
        }

        viewModelScope.launch {

            val personUid = accountManager.activeAccount?.userGuid ?: return@launch

            schoolDataSource.bookmarkDataSource
                .listAsFlow(
                    loadParams = DataLoadParams(),
                    listParams = BookmarkDataSource.GetListParams(
                        personUid = personUid
                    )
                )
                .collect { state ->

                    val bookmarks = state.dataOrNull() ?: emptyList()

                    loadMissingBookmarks(personUid)

                    val bookmarkDetails = loadApps(bookmarks)

                    _uiState.update {
                        it.copy(bookmarkDetails = bookmarkDetails)
                    }
                }
        }
    }

    fun onClickRemoveBookmark(bookmark: Bookmark) {
        viewModelScope.launch {

            val updatedBookmark = bookmark.copy(
                status = StatusEnum.TO_BE_DELETED
            )

            schoolDataSource.bookmarkDataSource.store(listOf(updatedBookmark))

            _uiState.update {
                it.copy(
                    bookmarkDetails = it.bookmarkDetails.filterNot {
                        b -> b.bookmark.learningUnitManifestUrl == bookmark.learningUnitManifestUrl
                    }
                )
            }
        }
    }

    private suspend fun loadMissingBookmarks(personUid: String) {
        val missingBookmarks = schoolDataSource.bookmarkDataSource
            .findBookmarksWithMissingPublication(personUid)

        missingBookmarks.forEach { bookmark ->
            schoolDataSource.opdsPublicationDataSource.getByUrl(
                url = bookmark.learningUnitManifestUrl,
                params = DataLoadParams(),
                referrerUrl = null,
                expectedPublicationId = null
            )
        }
    }

    private suspend fun loadApps(bookmarks: List<Bookmark>): List<BookmarkDetails> = coroutineScope {
        bookmarks.map { bookmark ->
            async {
                val app = schoolDataSource.opdsPublicationDataSource
                    .getByUrl(
                        url = bookmark.appManifestUrl,
                        params = DataLoadParams(),
                        referrerUrl = null,
                        expectedPublicationId = null
                    )
                BookmarkDetails(bookmark, app)
            }
        }.awaitAll()
    }

    fun onClickBookmark(bookmark: Bookmark) {
        _navCommandFlow.tryEmit(
            value = NavCommand.Navigate(
                LearningUnitDetail.create(
                    learningUnitManifestUrl = bookmark.learningUnitManifestUrl,
                    appManifestUrl = bookmark.appManifestUrl,
                )
            )
        )
    }
}

