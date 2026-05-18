package world.respect.shared.viewmodel.apps.enterlink

import androidx.lifecycle.SavedStateHandle
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.enter_link
import world.respect.shared.generated.resources.invalid_url
import world.respect.shared.resources.UiText
import world.respect.shared.navigation.AppsDetail
import world.respect.shared.resources.StringResourceUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.datalayer.DataErrorResult
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.school.opds.OpdsFeedDataSource
import world.respect.libutil.ext.RESPECT_SCHOOL_LINK_SEGMENT
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.sharelink.CreatePlaylistShareLinkUseCase
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.PlaylistDetail
import world.respect.shared.util.ext.asUiText
import kotlin.getValue

data class EnterLinkUiState(
    val linkUrl: String = "",
    val errorMessage: UiText? = null,
)

class EnterLinkViewModel(
    savedStateHandle: SavedStateHandle,
    accountManager: RespectAccountManager,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()

    private val schoolDataSource: SchoolDataSource by inject()

    private val _uiState = MutableStateFlow(EnterLinkUiState())

    val uiState = _uiState.asStateFlow()

    init {
        _appUiState.update {
            it.copy(
                title = Res.string.enter_link.asUiText(),
                hideBottomNavigation = true,
            )
        }
    }

    fun onLinkChanged(link: String) {
        _uiState.update {
            it.copy(
                linkUrl = link,
                errorMessage = null,
            )
        }
    }

    fun onClickNext() {
        launchWithLoadingIndicator {
            try {
                val linkUrl = Url(uiState.value.linkUrl)

                if (isPlaylistShareLink(linkUrl)) {
                    val playlistUrl = resolvePlaylistUrlFromShareLink(linkUrl)
                    _navCommandFlow.tryEmit(
                        NavCommand.Navigate(
                            PlaylistDetail.create(playlistUrl)
                        )
                    )
                    return@launchWithLoadingIndicator
                }

                val appResult = schoolDataSource.opdsPublicationDataSource.getByUrl(
                    url = linkUrl,
                    params = DataLoadParams(),
                    referrerUrl = null,
                    expectedPublicationId = null,
                )

                if(appResult is DataReadyState) {
                    _navCommandFlow.tryEmit(
                        NavCommand.Navigate(
                            AppsDetail.create(linkUrl)
                        )
                    )
                } else {
                    throw (appResult as? DataErrorResult)?.error ?: IllegalStateException()
                }
            } catch (_: Throwable) {
                _uiState.update {
                    it.copy(
                        errorMessage = StringResourceUiText(Res.string.invalid_url)
                    )
                }
            }
        }
    }

    private fun isPlaylistShareLink(url: Url): Boolean {
        val segments = url.pathSegments.filter { it.isNotBlank() }
        return segments.size >= 3 &&
                segments[segments.size - 3] == RESPECT_SCHOOL_LINK_SEGMENT &&
                segments[segments.size - 2] == CreatePlaylistShareLinkUseCase.PATH
    }

    private fun resolvePlaylistUrlFromShareLink(shareLink: Url): Url {
        val playlistUuid = shareLink.rawSegments.last { it.isNotBlank() }
        return URLBuilder(shareLink).apply {
            pathSegments = shareLink.pathSegments
                .filter { it.isNotBlank() }
                .dropLast(3) +
                    listOf(OpdsFeedDataSource.PLAYLIST_ENDPOINT_NAME, playlistUuid)
        }.build()
    }
    }

