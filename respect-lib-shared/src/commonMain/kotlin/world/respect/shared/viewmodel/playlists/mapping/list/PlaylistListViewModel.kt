package world.respect.shared.viewmodel.playlists.mapping.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.db.school.ext.isAdmin
import world.respect.datalayer.school.domain.MakePlaylistOpdsFeedUseCase
import world.respect.datalayer.school.opds.ext.selfUrl
import world.respect.lib.opds.model.OpdsFeed
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.home
import world.respect.shared.generated.resources.playlist
import world.respect.shared.navigation.EnterLink
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.PlaylistDetail
import world.respect.shared.navigation.PlaylistEdit
import world.respect.shared.navigation.PlaylistList
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.FabUiState

enum class PlaylistFilter {
    ALL,
    MY_PLAYLISTS,
}

data class PlaylistListUiState(
    val playlists: List<OpdsFeed> = emptyList(),
    val activeFilter: PlaylistFilter = PlaylistFilter.ALL,
    val isTeacherOrAdmin: Boolean = false,
    val activeUserOwnerHref: String = "",
    val isFabMenuExpanded: Boolean = false,
) {
    val showPlaylists: List<OpdsFeed>
        get() = when (activeFilter) {
            PlaylistFilter.ALL -> playlists
            PlaylistFilter.MY_PLAYLISTS -> playlists.filter { feed ->
                feed.links.any { link ->
                    link.rel?.contains(REL_OWNER) == true
                            && link.href == activeUserOwnerHref
                }
            }
        }

    companion object {
        val REL_OWNER = MakePlaylistOpdsFeedUseCase.REL_OWNER
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

    private val route: PlaylistList = savedStateHandle.toRoute()

    init {
        _appUiState.update {
            it.copy(title = Res.string.home.asUiText())
        }

        val activeAccount = accountManager.activeAccount
            ?: throw IllegalStateException(
                "No active account when initializing PlaylistListViewModel"
            )

        viewModelScope.launch {
            accountManager.selectedAccountAndPersonFlow.collect { sessionAndPerson ->
                val isTeacherOrAdmin = sessionAndPerson?.person?.isAdmin() == true

                val activeUserOwnerHref = sessionAndPerson?.let {
                    val username = it.person.username
                        ?.takeIf { name -> name.isNotBlank() }
                        ?: listOfNotNull(
                            it.person.givenName.takeIf { name -> name.isNotBlank() },
                            it.person.familyName.takeIf { name -> name.isNotBlank() },
                        ).joinToString(" ").takeIf { name -> name.isNotBlank() }
                        ?: it.session.account.userGuid

                    "${it.session.account.school.self}user/$username"
                } ?: ""
                _uiState.update {
                    it.copy(
                        isTeacherOrAdmin = isTeacherOrAdmin,
                        activeUserOwnerHref = activeUserOwnerHref,
                    )
                }

                _appUiState.update {
                    it.copy(
                        title = Res.string.home.asUiText(),
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
            schoolDataSource.opdsFeedDataSource.getPlaylistsAsFlow(
                schoolUrl = activeAccount.school.self
            ).collect { result ->
                when (result) {
                    is DataReadyState -> _uiState.update { it.copy(playlists = result.data) }
                    else -> {}
                }
            }
        }
    }

    fun onClickFilter(filter: PlaylistFilter) {
        _uiState.update { it.copy(activeFilter = filter) }
    }

    fun onClickPlaylist(feed: OpdsFeed) {
        val playlistUrl = feed.selfUrl()
            ?: throw IllegalStateException(
                "Playlist feed has no self URL: ${feed.metadata.title}"
            )
        val isPickMode = route.resultDest != null
        if (isPickMode) {
            _navCommandFlow.tryEmit(
                NavCommand.Navigate(
                    PlaylistDetail.create(
                        playlistUrl = playlistUrl,
                        resultDest = route.resultDest,
                    )
                )
            )
        } else {
            _navCommandFlow.tryEmit(
                NavCommand.Navigate(PlaylistDetail.create(playlistUrl = playlistUrl))
            )
        }
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