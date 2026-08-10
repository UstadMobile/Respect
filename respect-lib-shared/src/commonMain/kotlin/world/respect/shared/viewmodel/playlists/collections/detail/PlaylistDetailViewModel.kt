package world.respect.shared.viewmodel.playlists.collections.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.db.school.ext.isAdmin
import world.respect.datalayer.school.domain.MakePlaylistOpdsFeedUseCase
import world.respect.datalayer.school.opds.ext.selfUrl
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataReadyState
import world.respect.lib.opds.model.OpdsFeed
import world.respect.lib.opds.model.OpdsGroup
import world.respect.lib.opds.model.OpdsPublication
import world.respect.lib.opds.model.ReadiumLink
import world.respect.libutil.ext.resolve
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.openexternallink.OpenExternalLinkUseCase
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.edit
import world.respect.shared.navigation.AssignmentEdit
import world.respect.shared.navigation.LearningUnitDetail
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.NavResultReturner
import world.respect.shared.navigation.PlaylistDetail
import world.respect.shared.navigation.PlaylistEdit
import world.respect.shared.navigation.PlaylistShare
import world.respect.shared.navigation.RespectAppLauncher
import world.respect.shared.navigation.sendResultIfResultExpected
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.FabUiState
import world.respect.shared.viewmodel.learningunit.LearningUnitSelection
import world.respect.shared.viewmodel.learningunit.list.LearningUnitListViewModel
import kotlin.uuid.ExperimentalUuidApi

data class PlaylistDetailUiState(
    val feed: OpdsFeed? = null,
    val navigation: List<ReadiumLink> = emptyList(),
    val publications: List<OpdsPublication> = emptyList(),
    val group: List<OpdsGroup> = emptyList(),
    val isTeacherOrAdmin: Boolean = false,
    val collapsedSections: Set<String> = emptySet(),
    val showCopyDialog: Boolean = false,
    val copyDialogName: String = "",
    val showDeleteDialog: Boolean = false,
) {

    fun isSectionCollapsed(sectionKey: String) = sectionKey in collapsedSections

    val hasLearningUnitSections: Boolean
        get() = group.any { it.publications != null }

}

private fun PlaylistDetailUiState.withFeedContent(feed: OpdsFeed): PlaylistDetailUiState {
    return copy(
        feed = feed,
        navigation = feed.navigation ?: emptyList(),
        publications = feed.publications ?: emptyList(),
        group = feed.groups ?: emptyList(),
    )
}

class PlaylistDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val accountManager: RespectAccountManager,
    private val resultReturner: NavResultReturner,
    private val openExternalLinkUseCase: OpenExternalLinkUseCase,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()

    private val schoolDataSource: SchoolDataSource by inject()
    private val makePlaylistOpdsFeedUseCase: MakePlaylistOpdsFeedUseCase by inject()
    private val route: PlaylistDetail = savedStateHandle.toRoute()

    private val _uiState = MutableStateFlow(PlaylistDetailUiState())

    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            accountManager.selectedAccountAndPersonFlow.collect { sessionAndPerson ->
                val isTeacherOrAdmin = sessionAndPerson?.person?.isAdmin() == true
                _uiState.update { it.copy(isTeacherOrAdmin = isTeacherOrAdmin) }
                _appUiState.update {
                    it.copy(
                        fabState = FabUiState(
                            visible = isTeacherOrAdmin,
                            icon = FabUiState.FabIcon.EDIT,
                            text = Res.string.edit.asUiText(),
                            onClick = ::onClickEdit,
                        )
                    )
                }
            }
        }

        viewModelScope.launch {
            schoolDataSource.opdsFeedDataSource.getByUrlAsFlow(
                url = route.playlistUrl,
                params = DataLoadParams(),
            ).collect { result ->
                when (result) {
                    is DataReadyState -> {
                        _appUiState.update {
                            it.copy(title = result.data.metadata.title.asUiText())
                        }
                        _uiState.update { it.withFeedContent(result.data) }
                    }
                    else -> {}
                }
            }
        }
    }

    fun onClickToggleSection(sectionKey: String) {
        _uiState.update { prev ->
            val updatedCollapsed = if (sectionKey in prev.collapsedSections) {
                prev.collapsedSections - sectionKey
            } else {
                prev.collapsedSections + sectionKey
            }
            prev.copy(collapsedSections = updatedCollapsed)
        }
    }

    fun onClickShare() {
        val playlistUrl = _uiState.value.feed?.selfUrl()
            ?: throw IllegalStateException(
                "Cannot share playlist: feed has no self URL"
            )
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(PlaylistShare.create(playlistUrl = playlistUrl))
        )
    }

    fun onClickCopyPlaylist() {
        val feed = _uiState.value.feed
            ?: throw IllegalStateException("onClickCopyPlaylist called but feed is null")
        _uiState.update {
            it.copy(
                showCopyDialog = true,
                copyDialogName = feed.metadata.title,
            )
        }
    }

    fun onCopyDialogDismiss() {
        _uiState.update { it.copy(showCopyDialog = false, copyDialogName = "") }
    }

    fun onCopyDialogNameChanged(name: String) {
        _uiState.update { it.copy(copyDialogName = name) }
    }

    fun onCopyDialogConfirm() {
        val feed = _uiState.value.feed
            ?: throw IllegalStateException("onCopyDialogConfirm called but feed is null")
        val newName = _uiState.value.copyDialogName.trim()
        if (newName.isBlank())
            throw IllegalStateException(" newName is blank")

        viewModelScope.launch {
            val sessionAndPerson = accountManager.selectedAccountAndPersonFlow.first()
                ?: throw IllegalStateException("No active session when copying playlist")

            val username = sessionAndPerson.person.username
                ?: throw IllegalStateException(
                    "Active person has no username: ${sessionAndPerson.person.guid}"
                )

            @OptIn(ExperimentalUuidApi::class)
            val copiedFeed = makePlaylistOpdsFeedUseCase.invoke(
                base = feed.copy(
                    metadata = feed.metadata.copy(title = newName)
                ),
                username = username,
            )

            schoolDataSource.opdsFeedDataSource.store(listOf(copiedFeed))

            _uiState.update { it.copy(showCopyDialog = false, copyDialogName = "") }

            val copiedUrl = copiedFeed.selfUrl()
                ?: throw IllegalStateException("Copied feed has no self URL")

            _navCommandFlow.tryEmit(
                NavCommand.Navigate(
                    PlaylistEdit.create(
                        playlistUrl = copiedUrl,
                        isCopy = true,
                    )
                )
            )
        }
    }

    fun onClickDelete() {
        _uiState.update { it.copy(showDeleteDialog = true) }
    }

    fun onDeleteDialogDismiss() {
        _uiState.update { it.copy(showDeleteDialog = false) }
    }

    fun onDeleteDialogConfirm() {
        viewModelScope.launch {
            val feed = _uiState.value.feed
                ?: throw IllegalStateException("feed is null")
            val selfUrl = feed.selfUrl()
                ?: throw IllegalStateException("Cannot delete playlist: feed has no self URL")

            schoolDataSource.opdsFeedDataSource.deleteByUrl(selfUrl)

            _uiState.update { it.copy(showDeleteDialog = false) }
            _navCommandFlow.tryEmit(
                NavCommand.Navigate(
                    destination = RespectAppLauncher.create()
                )
            )
        }
    }

    fun onClickNavigation(navigation: ReadiumLink) {
        val resolvedUrl = route.playlistUrl.resolve(navigation.href)
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                PlaylistDetail.create(playlistUrl = resolvedUrl)
            )
        )
    }

    fun onClickPublication(publication: OpdsPublication) {
        val selfLink = publication.links.find {
            it.rel?.contains(LearningUnitListViewModel.SELF) == true
        } ?: throw IllegalStateException(
            "Publication has no self link: ${publication.metadata.title}"
        )

        val manifestUrl = route.playlistUrl.resolve(selfLink.href)

        val resultSent = resultReturner.sendResultIfResultExpected(
            route = route,
            navCommandFlow = _navCommandFlow,
            result = LearningUnitSelection(
                learningUnitManifestUrl = manifestUrl,
                selectedPublication = publication,
            )
        )
        if (resultSent) return

        if (selfLink.type == MIME_TYPE_HTML) {
            viewModelScope.launch {
                openExternalLinkUseCase(url = route.playlistUrl.resolve(selfLink.href))
            }
            return
        }

        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                LearningUnitDetail.create(
                    learningUnitManifestUrl = manifestUrl,
                    expectedIdentifier = publication.metadata.identifier?.toString(),
                )
            )
        )
    }

    fun onClickAssignSection(sectionIndex: Int) {
        val feed = _uiState.value.feed ?: throw IllegalStateException(
            "Cannot assign: no playlist feed loaded"
        )
        val playlistUrl = feed.selfUrl()
            ?: throw IllegalStateException("Cannot assign: playlist feed has no self URL")

        val targetSection = if (sectionIndex == ASSIGN_HEADER_SECTION_INDEX) {
            _uiState.value.group.firstOrNull { it.publications?.isNotEmpty() == true }
                ?: throw IllegalStateException("No learning unit section with items found to assign")
        } else {
            _uiState.value.group.getOrNull(sectionIndex)
                ?: throw IllegalStateException("No section at index $sectionIndex")
        }

        val firstPublication = targetSection.publications?.firstOrNull()
            ?: throw IllegalStateException(
                "Assign clicked but section at index $sectionIndex has no learning items"
            )

        val publicationSelfHref = firstPublication.links.find {
            it.rel?.contains(LearningUnitListViewModel.SELF) == true
        }?.href ?: throw IllegalStateException(
            "Publication has no self link: ${firstPublication.metadata.title}"
        )

        val learningUnitManifestUrl = playlistUrl.resolve(publicationSelfHref)

        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                destination = AssignmentEdit.create(
                    assignmentActivityId = null,
                    learningUnitSelected = LearningUnitSelection(
                        learningUnitManifestUrl = learningUnitManifestUrl,
                        selectedPublication = firstPublication,
                    )
                )
            )
        )
    }

    fun onClickEdit() {
        val playlistUrl = _uiState.value.feed?.selfUrl()
            ?: throw IllegalStateException(
                "Cannot edit playlist: feed has no self URL"
            )
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(PlaylistEdit.create(playlistUrl = playlistUrl))
        )
    }

    companion object {
        const val ASSIGN_HEADER_SECTION_INDEX = -1
        private const val MIME_TYPE_HTML = "text/html"
    }
}