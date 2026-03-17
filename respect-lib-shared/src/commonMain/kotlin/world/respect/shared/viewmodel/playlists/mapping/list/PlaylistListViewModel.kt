package world.respect.shared.viewmodel.playlists.mapping.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import io.ktor.http.Url
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.db.school.ext.isAdmin
import world.respect.datalayer.school.opds.OpdsFeedDataSource
import world.respect.lib.opds.model.OpdsPublication
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.home
import world.respect.shared.generated.resources.playlist
import world.respect.shared.navigation.EnterLink
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.PlaylistDetail
import world.respect.shared.navigation.PlaylistEdit
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.FabUiState

enum class PlaylistFilter {
    ALL,
    MY_PLAYLISTS,
}

data class PlaylistListUiState(
    val playlists: List<OpdsPublication> = emptyList(),
    val activeFilter: PlaylistFilter = PlaylistFilter.ALL,
    val isTeacherOrAdmin: Boolean = false,
    val activeUserOwnerHref: String = "",
    val isFabMenuExpanded: Boolean = false,
) {
    /**
     * user's owner href. Owner is identified via:
     * rel = "https://respect.ustadmobile.com/ns/owner"
     * href = "{schoolBaseUrl}user/{userGuid}"
     */
    val showPlaylists: List<OpdsPublication>
        get() = when (activeFilter) {
            PlaylistFilter.ALL -> playlists
            PlaylistFilter.MY_PLAYLISTS -> playlists.filter { publication ->
                publication.links.any { link ->
                    link.rel?.contains(REL_OWNER) == true
                            && link.href == activeUserOwnerHref
                }
            }
        }

    companion object {
        const val REL_OWNER = "https://respect.ustadmobile.com/ns/owner"
        const val REL_SELF = "self"
    }
}

class PlaylistListViewModel(
    savedStateHandle: SavedStateHandle,
    private val accountManager: RespectAccountManager,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()

    private val schoolDataSource: SchoolDataSource by inject()

    private val _uiState = MutableStateFlow(PlaylistListUiState())

    val uiState = _uiState.asStateFlow()

    init {
        _appUiState.update {
            it.copy(title = Res.string.home.asUiText())
        }
        val activeAccount = accountManager.activeAccount
            ?: throw IllegalStateException(
            )

        val playlistFeedUrl = Url(
            "${activeAccount.school.self}${OpdsFeedDataSource.PLAYLIST_ENDPOINT_NAME}"
        )

        viewModelScope.launch {
            accountManager.selectedAccountAndPersonFlow.collect { sessionAndPerson ->
                val isTeacherOrAdmin = sessionAndPerson?.person?.isAdmin() == true

                val activeUserOwnerHref = sessionAndPerson?.session?.account?.let { account ->
                    "${account.school.self}user/${account.userGuid}"
                } ?: ""

                _uiState.update {
                    it.copy(
                        isTeacherOrAdmin = isTeacherOrAdmin,
                        activeUserOwnerHref = activeUserOwnerHref,
                    )
                }

                _appUiState.update {
                    it.copy(
                        fabState = FabUiState(
                            visible = isTeacherOrAdmin,
                            icon = FabUiState.FabIcon.ADD,
                            text = Res.string.playlist.asUiText(),
                            onClick = ::onClickCreatePlaylist,
                        )
                    )
                }
            }
        }

        viewModelScope.launch {
            schoolDataSource.opdsFeedDataSource.getByUrlAsFlow(
                url = playlistFeedUrl,
                params = DataLoadParams()
            ).collect { result ->
                when (result) {
                    is DataReadyState -> {
                        _uiState.update {
                            it.copy(playlists = result.data.publications ?: emptyList())
                        }
                    }
                    else -> {

                    }
                }
            }
        }
    }

    fun onClickFilter(filter: PlaylistFilter) {
        _uiState.update { it.copy(activeFilter = filter) }
    }

    fun onClickPlaylist(publication: OpdsPublication) {
        val selfHref = publication.links.find {
            it.rel?.contains(PlaylistListUiState.REL_SELF) == true
        }?.href ?: throw IllegalStateException(
            "Playlist publication has no self link: ${publication.metadata.title}"
        )
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                PlaylistDetail.create(playlistUrl = Url(selfHref))
            )
        )
    }

    fun onClickCreatePlaylist() {
        _uiState.update { it.copy(isFabMenuExpanded = !it.isFabMenuExpanded) }
    }

    fun onClickDismissFabMenu() {
        _uiState.update { it.copy(isFabMenuExpanded = false) }
    }

    fun onClickAddNew() {
        _uiState.update { it.copy(isFabMenuExpanded = false) }
        _navCommandFlow.tryEmit(NavCommand.Navigate(PlaylistEdit.create()))
    }

    fun onClickAddFromLink() {
        _uiState.update { it.copy(isFabMenuExpanded = false) }
        _navCommandFlow.tryEmit(NavCommand.Navigate(EnterLink))
    }
}