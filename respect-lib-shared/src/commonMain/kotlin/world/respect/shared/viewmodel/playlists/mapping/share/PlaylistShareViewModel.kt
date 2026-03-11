package world.respect.shared.viewmodel.playlists.mapping.share

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import io.ktor.http.Url
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.scope.Scope
import world.respect.libutil.ext.appendEndpointSegments
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.navigation.PlaylistShare
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.share


data class PlaylistShareUiState(
    val playlistUid: Long = 0L,
    val shareUrl: Url? = null,
    val viewPermission: String = "",
    val editPermission: String = "",
)

class PlaylistShareViewModel(
    savedStateHandle: SavedStateHandle,
    private val accountManager: RespectAccountManager,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()

    private val _uiState = MutableStateFlow(PlaylistShareUiState())

    val uiState = _uiState.asStateFlow()

    private val route: PlaylistShare = savedStateHandle.toRoute()

    init {
        _appUiState.update {
            it.copy(
                title = Res.string.share.asUiText(),
                hideBottomNavigation = true,
            )
        }

        val playlistUid = route.playlistUid

        _uiState.update {
            it.copy(
                playlistUid = playlistUid,
                viewPermission = "",
                editPermission = ""
            )
        }

        viewModelScope.launch {
            accountManager.selectedAccountAndPersonFlow.collect { sessionAndPerson ->
                val schoolUrl = sessionAndPerson?.session?.account?.school?.self

                if (schoolUrl != null) {
                    val shareUrl = buildShareUrl(schoolUrl, playlistUid)
                    _uiState.update { prev ->
                        prev.copy(shareUrl = shareUrl)
                    }
                }
            }
        }
    }

    private fun buildShareUrl(schoolUrl: Url, playlistUid: Long): Url {
        return schoolUrl.appendEndpointSegments("playlist", playlistUid.toString())
    }

    fun onClickShareLink() {
        // TODO:
    }

    fun onClickCopyLink() {
        // TODO:
    }

    fun onClickSendViaSms() {
        // TODO:
    }

    fun onClickSendViaEmail() {
        // TODO:
    }

    fun onViewPermissionChanged(permission: String) {
        _uiState.update { it.copy(viewPermission = permission) }
    }

    fun onEditPermissionChanged(permission: String) {
        _uiState.update { it.copy(editPermission = permission) }
    }
}