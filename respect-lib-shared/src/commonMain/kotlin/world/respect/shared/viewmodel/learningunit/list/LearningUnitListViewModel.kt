package world.respect.shared.viewmodel.learningunit.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import io.ktor.http.Url
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.db.school.ext.isAdmin
import world.respect.datalayer.ext.dataOrNull
import world.respect.datalayer.school.domain.MakePlaylistOpdsFeedUseCase
import world.respect.datalayer.school.opds.OpdsFeedDataSource
import world.respect.datalayer.school.opds.ext.selfUrl
import world.respect.lib.opds.model.OpdsFacet
import world.respect.lib.opds.model.OpdsFeed
import world.respect.lib.opds.model.OpdsFeedMetadata
import world.respect.lib.opds.model.OpdsGroup
import world.respect.lib.opds.model.OpdsPublication
import world.respect.lib.opds.model.ReadiumLink
import world.respect.libutil.ext.resolve
import world.respect.libutil.util.time.systemTimeInMillis
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.edit
import world.respect.shared.generated.resources.language
import world.respect.shared.navigation.AssignmentEdit
import world.respect.shared.navigation.LearningUnitDetail
import world.respect.shared.navigation.LearningUnitList
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.NavResultReturner
import world.respect.shared.navigation.PlaylistDetail
import world.respect.shared.navigation.PlaylistEdit
import world.respect.shared.navigation.PlaylistShare
import world.respect.shared.navigation.sendResultIfResultExpected
import world.respect.shared.util.SortOrderOption
import world.respect.shared.util.ext.asUiText
import world.respect.shared.util.ext.resolve
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.AppBarSearchUiState
import world.respect.shared.viewmodel.app.appstate.FabUiState
import world.respect.shared.viewmodel.learningunit.LearningUnitSelection
import world.respect.shared.viewmodel.playlists.mapping.edit.PlaylistEditViewModel
import world.respect.shared.viewmodel.playlists.mapping.list.PlaylistListUiState
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi

data class LearningUnitListUiState(
    val publications: List<OpdsPublication> = emptyList(),
    val navigation: List<ReadiumLink> = emptyList(),
    val group: List<OpdsGroup> = emptyList(),
    val facetOptions: List<OpdsFacet> = emptyList(),
    val selectedFilterTitle: String? = null,
    val sortOptions: List<SortOrderOption> = emptyList(),
    val activeSortOrderOption: SortOrderOption = SortOrderOption(
        Res.string.language, 1, true
    ),
    val fieldsEnabled: Boolean = true,
    val feed: OpdsFeed? = null,
    val isTeacherOrAdmin: Boolean = false,
    val collapsedSections: Set<String> = emptySet(),
    val isMultiSelectMode: Boolean = false,
    val selectedPublications: Set<String> = emptySet(),
    val showCopyDialog: Boolean = false,
    val copyDialogName: String = "",
    val showDeleteDialog: Boolean = false,
    val showSelectPlaylistButton: Boolean = false,
    val selectedNavigationHref: String? = null,
) {
    fun isSectionCollapsed(sectionKey: String) = sectionKey in collapsedSections

    fun isPublicationSelected(publication: OpdsPublication): Boolean =
        publication.metadata.identifier?.toString() in selectedPublications

    fun isNavigationSelected(navigation: ReadiumLink): Boolean =
        navigation.href == selectedNavigationHref

    val selectedCount: Int
        get() = selectedPublications.size

    val hasLearningUnitSections: Boolean
        get() = group.any { it.publications != null }
}

private fun LearningUnitListUiState.withFeedContent(feed: OpdsFeed): LearningUnitListUiState {
    return copy(
        feed = feed,
        navigation = feed.navigation ?: emptyList(),
        publications = feed.publications ?: emptyList(),
        group = feed.groups ?: emptyList(),
    )
}

class LearningUnitListViewModel(
    savedStateHandle: SavedStateHandle,
    private val resultReturner: NavResultReturner,
    accountManager: RespectAccountManager,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()

    private val _uiState = MutableStateFlow(LearningUnitListUiState())

    val uiState = _uiState.asStateFlow()

    private val route: LearningUnitList = savedStateHandle.toRoute()

    private val schoolDataSource: SchoolDataSource by inject()

    init {
        _uiState.update {
            it.copy(
                showSelectPlaylistButton = route.resultDest?.resultKey == PlaylistEditViewModel.KEY_PLAYLIST
            )
        }
        viewModelScope.launch {
            _appUiState.update {
                it.copy(searchState = AppBarSearchUiState(visible = true))
            }

            schoolDataSource.opdsFeedDataSource.getByUrlAsFlow(
                url = route.opdsFeedUrl,
                params = DataLoadParams()
            ).collect { result ->
                when (result) {
                    is DataReadyState -> {
                        val resolvedFeed = result.data.resolve(route.opdsFeedUrl)
                        val facetOptions = result.data.facets ?: emptyList()
                        val sortOptions = facetOptions.mapIndexed { index, _ ->
                            SortOrderOption(
                                fieldMessageId = Res.string.language,
                                flag = index + 1,
                                order = true
                            )
                        }

                        _appUiState.update {
                            it.copy(
                                title = result.data.metadata.title.asUiText(),
                                searchState = AppBarSearchUiState(visible = true)
                            )
                        }

                        _uiState.update {
                            it.withFeedContent(resolvedFeed).copy(
                                facetOptions = facetOptions,
                                sortOptions = sortOptions,
                            )
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    fun onSortOrderChanged(sortOption: SortOrderOption) {
        _uiState.update { it.copy(activeSortOrderOption = sortOption) }
    }
    fun onClickPublication(publication: OpdsPublication) {
        if (_uiState.value.isMultiSelectMode) {
            toggleSelection(publication)
            return
        }

        val publicationHref = publication.links.find {
            it.rel?.contains(SELF) == true
        }?.href.toString()

        val refererUrl = route.opdsFeedUrl.resolve(publicationHref).toString()
        val learningUnitManifestUrl = route.opdsFeedUrl.resolve(publicationHref)

        if (
            !resultReturner.sendResultIfResultExpected(
                route = route,
                navCommandFlow = _navCommandFlow,
                result = LearningUnitSelection(
                    learningUnitManifestUrl = learningUnitManifestUrl,
                    selectedPublication = publication,
                    appManifestUrl = route.appManifestUrl,
                )
            )
        ) {
            _navCommandFlow.tryEmit(
                value = NavCommand.Navigate(
                    LearningUnitDetail.create(
                        learningUnitManifestUrl = learningUnitManifestUrl,
                        appManifestUrl = route.appManifestUrl,
                        refererUrl = Url(refererUrl),
                        expectedIdentifier = publication.metadata.identifier.toString()
                    )
                )
            )
        }
    }
    fun onLongPressPublication(publication: OpdsPublication) {
        _uiState.update { it.copy(isMultiSelectMode = true) }
        toggleSelection(publication)
    }

    private fun toggleSelection(publication: OpdsPublication) {
        val id = publication.metadata.identifier?.toString() ?: return
        _uiState.update { prev ->
            val updated = if (id in prev.selectedPublications) {
                prev.selectedPublications - id
            } else {
                prev.selectedPublications + id
            }
            prev.copy(
                selectedPublications = updated,
                isMultiSelectMode = updated.isNotEmpty(),
            )
        }
    }

    fun onClickConfirmSelection() {
        val currentState = _uiState.value
        if (currentState.selectedPublications.isEmpty()) return

        val allPublications = currentState.publications +
                currentState.group.flatMap { it.publications ?: emptyList() }

        val selections = allPublications
            .filter { pub ->
                pub.metadata.identifier?.toString() in currentState.selectedPublications
            }
            .map { publication ->
                val publicationHref = publication.links.find {
                    it.rel?.contains(SELF) == true
                }?.href.toString()
                LearningUnitSelection(
                    learningUnitManifestUrl = route.opdsFeedUrl.resolve(publicationHref),
                    selectedPublication = publication,
                    appManifestUrl = route.appManifestUrl,
                )
            }

        resultReturner.sendResultIfResultExpected(
            route = route,
            navCommandFlow = _navCommandFlow,
            result = selections,
        )
    }

    fun onClickNavigation(navigation: ReadiumLink) {
        val navigationHref = navigation.href
        val resolvedUrl = route.opdsFeedUrl.resolve(navigationHref)

        // In playlist-pick mode, clicking a navigation item (grade) selects it.
        // The user then confirms by clicking the "Select Playlist" button.
        if (route.resultDest?.resultKey == PlaylistEditViewModel.KEY_PLAYLIST) {
            _uiState.update { prev ->
                prev.copy(
                    selectedNavigationHref = if (prev.selectedNavigationHref == resolvedUrl.toString()) {
                        null // deselect if already selected
                    } else {
                        resolvedUrl.toString()
                    }
                )
            }
            return
        }

        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                LearningUnitList.create(
                    opdsFeedUrl = resolvedUrl,
                    appManifestUrl = route.appManifestUrl,
                    resultDest = route.resultDest,
                )
            )
        )
    }

    fun onClickSelectPlaylist() {
        val selectedHref = _uiState.value.selectedNavigationHref
            ?: return // no grade selected, do nothing

        resultReturner.sendResultIfResultExpected(
            route = route,
            navCommandFlow = _navCommandFlow,
            result = selectedHref,
        )
    }

    companion object {
        const val SELF = "self"
        const val ICON = "icon"
    }
}

class PlaylistDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val accountManager: RespectAccountManager,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()

    private val schoolDataSource: SchoolDataSource by inject()

    private val route: PlaylistDetail = savedStateHandle.toRoute()

    private val _uiState = MutableStateFlow(LearningUnitListUiState())

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
        val feed = _uiState.value.feed ?: return
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
        val feed = _uiState.value.feed ?: return
        val newName = _uiState.value.copyDialogName.trim()
        if (newName.isBlank()) return

        viewModelScope.launch {
            val activeAccount = accountManager.activeAccount
                ?: throw IllegalStateException("No active account when copying playlist")

            val sessionAndPerson = accountManager.selectedAccountAndPersonFlow
                .first { it != null }

            val username = sessionAndPerson?.person?.username
                ?.takeIf { it.isNotBlank() }
                ?: listOfNotNull(
                    sessionAndPerson?.person?.givenName?.takeIf { it.isNotBlank() },
                    sessionAndPerson?.person?.familyName?.takeIf { it.isNotBlank() },
                ).joinToString(" ").takeIf { it.isNotBlank() }
                ?: sessionAndPerson?.person?.guid
                ?: throw IllegalStateException("No username available when copying playlist")

            @OptIn(ExperimentalUuidApi::class)
            val copiedFeed = MakePlaylistOpdsFeedUseCase(
                schoolUrl = activeAccount.school.self
            ).invoke(
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
            val feed = _uiState.value.feed ?: return@launch
            val selfHref = feed.selfUrl()?.toString() ?: return@launch

            val activeAccount = accountManager.activeAccount
                ?: throw IllegalStateException(
                    "No active account when deleting playlist"
                )

            val playlistListUrl = Url(
                "${activeAccount.school.self}${OpdsFeedDataSource.PLAYLIST_ENDPOINT_NAME}"
            )

            val existingListFeed = schoolDataSource.opdsFeedDataSource
                .getByUrl(url = playlistListUrl, params = DataLoadParams())
                .dataOrNull()

            if (existingListFeed != null) {
                val updatedPublications = (existingListFeed.publications ?: emptyList())
                    .filter { pub ->
                        pub.links.none { link ->
                            link.rel?.contains(PlaylistListUiState.REL_SELF) == true &&
                                    link.href == selfHref
                        }
                    }

                val updatedListFeed = OpdsFeed(
                    metadata = OpdsFeedMetadata(
                        title = existingListFeed.metadata.title,
                        modified = Instant.fromEpochMilliseconds(systemTimeInMillis()),
                    ),
                    links = existingListFeed.links,
                    publications = updatedPublications,
                    groups = existingListFeed.groups ?: emptyList(),
                )

                schoolDataSource.opdsFeedDataSource.store(listOf(updatedListFeed))
            }

            _uiState.update { it.copy(showDeleteDialog = false) }
            _navCommandFlow.tryEmit(NavCommand.PopUp())
        }
    }

    fun onClickNavigation(navigation: ReadiumLink) {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                PlaylistDetail.create(playlistUrl = Url(navigation.href))
            )
        )
    }

    fun onClickPublication(publication: OpdsPublication) {
        val selfHref = publication.links.find {
            it.rel?.contains(LearningUnitListViewModel.SELF) == true
        }?.href ?: throw IllegalStateException(
            "Publication has no self link: ${publication.metadata.title}"
        )

        val appManifestUrl = _uiState.value.feed?.selfUrl()
            ?: throw IllegalStateException(
                "Cannot navigate to publication: playlist feed has no self URL"
            )

        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                LearningUnitDetail.create(
                    learningUnitManifestUrl = Url(selfHref),
                    appManifestUrl = appManifestUrl,
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
            _uiState.value.group.firstOrNull { it.publications != null }
                ?: throw IllegalStateException("No learning unit section found to assign")
        } else {
            _uiState.value.group.getOrNull(sectionIndex)
                ?: throw IllegalStateException("No section at index $sectionIndex")
        }

        val firstPublication = targetSection.publications?.firstOrNull()
            ?: throw IllegalStateException(
                "Assign clicked but section at index $sectionIndex has no learning items"
            )

        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                destination = AssignmentEdit.create(
                    uid = null,
                    learningUnitSelected = LearningUnitSelection(
                        learningUnitManifestUrl = playlistUrl,
                        selectedPublication = firstPublication,
                        appManifestUrl = playlistUrl,
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
    }
}