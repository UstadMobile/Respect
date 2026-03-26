package world.respect.shared.viewmodel.playlists.mapping.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import io.ktor.http.Url
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.ext.dataOrNull
import world.respect.datalayer.school.domain.MakePlaylistOpdsFeedUseCase
import world.respect.datalayer.school.opds.OpdsFeedDataSource
import world.respect.datalayer.school.opds.ext.selfUrl
import world.respect.lib.opds.model.OpdsFeed
import world.respect.lib.opds.model.OpdsFeedMetadata
import world.respect.lib.opds.model.OpdsGroup
import world.respect.lib.opds.model.OpdsPublication
import world.respect.lib.opds.model.LangMapStringValue
import world.respect.lib.opds.model.ReadiumLink
import world.respect.lib.opds.model.ReadiumMetadata
import world.respect.libutil.util.time.systemTimeInMillis
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.add_playlist
import world.respect.shared.generated.resources.copy_playlist
import world.respect.shared.generated.resources.edit_playlist
import world.respect.shared.generated.resources.learning_item_section
import world.respect.shared.generated.resources.playlist_section
import world.respect.shared.generated.resources.save
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.NavResultReturner
import world.respect.shared.navigation.PlaylistDetail
import world.respect.shared.navigation.PlaylistEdit
import world.respect.shared.navigation.PlaylistList
import world.respect.shared.navigation.RespectAppLauncher
import world.respect.shared.navigation.RouteResultDest
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.ActionBarButtonUiState
import world.respect.shared.viewmodel.learningunit.LearningUnitSelection
import world.respect.shared.viewmodel.playlists.mapping.list.PlaylistListUiState
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi

enum class PlaylistSectionType {
    NAVIGATION,
    PUBLICATION,
}

data class MovingItemState(
    val fromSectionIndex: Int,
    val itemIndex: Int,
    val compatibleSections: List<CompatibleSection>,
) {
    data class CompatibleSection(
        val sectionIndex: Int,
        val title: String,
        val itemCount: Int,
    )
}

data class PlaylistEditUiState(
    val feed: OpdsFeed? = null,
    val isSectionTypeDialogVisible: Boolean = false,
    val titleError: Boolean = false,
    val movingItem: MovingItemState? = null,
) {
    val title: String
        get() = feed?.metadata?.title ?: ""

    val description: String
        get() = feed?.metadata?.description ?: ""

    val sections: List<OpdsGroup>
        get() = feed?.groups ?: emptyList()
}

class PlaylistEditViewModel(
    savedStateHandle: SavedStateHandle,
    private val accountManager: RespectAccountManager,
    private val resultReturner: NavResultReturner,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()

    private val schoolDataSource: SchoolDataSource by inject()

    private val route: PlaylistEdit = savedStateHandle.toRoute()

    private val _uiState = MutableStateFlow(PlaylistEditUiState())

    val uiState = _uiState.asStateFlow()

    private var pendingAddItemSectionIndex: Int?
        get() = savedStateHandle.get<Int>(KEY_PENDING_ADD_ITEM_SECTION_INDEX)
        set(value) { savedStateHandle[KEY_PENDING_ADD_ITEM_SECTION_INDEX] = value }

    private var pendingAddPlaylistSectionIndex: Int?
        get() = savedStateHandle.get<Int>(KEY_PENDING_ADD_PLAYLIST_SECTION_INDEX)
        set(value) { savedStateHandle[KEY_PENDING_ADD_PLAYLIST_SECTION_INDEX] = value }

    init {
        val existingPlaylistUrl = route.playlistUrl
        if (existingPlaylistUrl != null) {
            viewModelScope.launch {
                schoolDataSource.opdsFeedDataSource.getByUrlAsFlow(
                    url = existingPlaylistUrl,
                    params = DataLoadParams(),
                ).collect { result ->
                    when (result) {
                        is DataReadyState -> {
                            _uiState.update { it.copy(feed = result.data) }
                        }
                        else -> {}
                    }
                }
            }
        } else {
            viewModelScope.launch {
                val activeAccount = accountManager.activeAccount
                    ?: throw IllegalStateException(
                        "No active account when initializing PlaylistEditViewModel"
                    )

                val sessionAndPerson = accountManager.selectedAccountAndPersonFlow
                    .first { it != null }

                val username = sessionAndPerson?.person?.username
                    ?.takeIf { it.isNotBlank() }
                    ?: listOfNotNull(
                        sessionAndPerson?.person?.givenName?.takeIf { it.isNotBlank() },
                        sessionAndPerson?.person?.familyName?.takeIf { it.isNotBlank() },
                    ).joinToString(" ").takeIf { it.isNotBlank() }
                    ?: activeAccount.userGuid

                @OptIn(ExperimentalUuidApi::class)
                _uiState.update {
                    it.copy(
                        feed = MakePlaylistOpdsFeedUseCase(
                            schoolUrl = activeAccount.school.self
                        ).invoke(
                            base = OpdsFeed(
                                metadata = OpdsFeedMetadata(title = ""),
                                links = emptyList(),
                                publications = emptyList(),
                                groups = emptyList(),
                            ),
                            username = username,
                        )
                    )
                }
                restoreAppBarState()
            }
        }

        viewModelScope.launch {
            resultReturner.filteredResultFlowForKey(KEY_LEARNING_UNIT).collect { result ->
                val sectionIndex = pendingAddItemSectionIndex
                    ?: throw IllegalStateException(
                        "Received learning unit result but no pending section index"
                    )
                pendingAddItemSectionIndex = null

                val selections: List<LearningUnitSelection> = when (val r = result.result) {
                    is LearningUnitSelection -> listOf(r)
                    is List<*> -> r.filterIsInstance<LearningUnitSelection>()
                    else -> throw IllegalStateException(
                        "Expected LearningUnitSelection or List but got: ${result.result}"
                    )
                }
                _uiState.first { it.feed != null }

                _uiState.update { prev ->
                    val sections = (prev.feed?.groups ?: emptyList()).toMutableList()
                    val section = sections.getOrNull(sectionIndex)
                        ?: throw IllegalStateException("No section at index $sectionIndex")
                    sections[sectionIndex] = section.copy(
                        publications = (section.publications ?: emptyList()) +
                                selections.map { it.selectedPublication }
                    )
                    prev.copy(feed = prev.feed?.copy(groups = sections))
                }
                restoreAppBarState()
            }
        }

        viewModelScope.launch {
            resultReturner.filteredResultFlowForKey(KEY_PLAYLIST).collect { result ->
                val sectionIndex = pendingAddPlaylistSectionIndex
                    ?: throw IllegalStateException(
                        "Received playlist result but no pending section index"
                    )
                pendingAddPlaylistSectionIndex = null

                val selfHref = result.result as? String
                    ?: throw IllegalStateException(
                        "Expected String playlist href but got: ${result.result}"
                    )

                val playlistTitle = schoolDataSource.opdsFeedDataSource
                    .getByUrl(url = Url(selfHref), params = DataLoadParams())
                    .dataOrNull()
                    ?.metadata
                    ?.title

                val navLink = ReadiumLink(
                    href = selfHref,
                    rel = listOf(PlaylistListUiState.REL_SELF),
                    type = OpdsFeed.MEDIA_TYPE,
                    title = playlistTitle,
                )
                _uiState.first { it.feed != null }

                _uiState.update { prev ->
                    val sections = (prev.feed?.groups ?: emptyList()).toMutableList()
                    val section = sections.getOrNull(sectionIndex)
                        ?: throw IllegalStateException("No section at index $sectionIndex")
                    sections[sectionIndex] = section.copy(
                        navigation = (section.navigation ?: emptyList()) + navLink
                    )
                    prev.copy(feed = prev.feed?.copy(groups = sections))
                }
                restoreAppBarState()
            }
        }
    }

    fun restoreAppBarState() {
        _appUiState.update { prev ->
            prev.copy(
                title = when {
                    route.isCopy -> Res.string.copy_playlist.asUiText()
                    route.playlistUrl == null -> Res.string.add_playlist.asUiText()
                    else -> Res.string.edit_playlist.asUiText()
                },
                userAccountIconVisible = false,
                actionBarButtonState = ActionBarButtonUiState(
                    visible = true,
                    text = Res.string.save.asUiText(),
                    onClick = ::onClickSave,
                ),
                hideBottomNavigation = true,
            )
        }
    }

    fun onTitleChanged(title: String) {
        _uiState.update { prev ->
            prev.copy(
                feed = prev.feed?.copy(
                    metadata = prev.feed.metadata.copy(title = title)
                ),
                titleError = false,
            )
        }
    }

    fun onDescriptionChanged(description: String) {
        _uiState.update { prev ->
            prev.copy(
                feed = prev.feed?.copy(
                    metadata = prev.feed.metadata.copy(description = description)
                )
            )
        }
    }

    fun onSectionTitleChanged(sectionIndex: Int, title: String) {
        _uiState.update { prev ->
            val sections = (prev.feed?.groups ?: emptyList()).toMutableList()
            val section = sections.getOrNull(sectionIndex)
                ?: throw IllegalStateException("No section at index $sectionIndex")
            sections[sectionIndex] = section.copy(
                metadata = section.metadata.copy(title = title)
            )
            prev.copy(feed = prev.feed?.copy(groups = sections))
        }
    }

    fun onClickAddSection() {
        _uiState.update { it.copy(isSectionTypeDialogVisible = true) }
    }

    fun onDismissSectionTypeDialog() {
        _uiState.update { it.copy(isSectionTypeDialogVisible = false) }
    }

    fun onClickSectionType(sectionType: PlaylistSectionType) {
        viewModelScope.launch {
            val sectionTitle = when (sectionType) {
                PlaylistSectionType.NAVIGATION -> getString(Res.string.playlist_section)
                PlaylistSectionType.PUBLICATION -> getString(Res.string.learning_item_section)
            }
            _uiState.update { prev ->
                val newSection = OpdsGroup(
                    metadata = OpdsFeedMetadata(title = sectionTitle),
                    navigation = if (sectionType == PlaylistSectionType.NAVIGATION) emptyList() else null,
                    publications = if (sectionType == PlaylistSectionType.PUBLICATION) emptyList() else null,
                )
                prev.copy(
                    feed = prev.feed?.copy(
                        groups = (prev.feed.groups ?: emptyList()) + newSection
                    ),
                    isSectionTypeDialogVisible = false,
                )
            }
        }
    }

    fun onClickDeleteSection(sectionIndex: Int) {
        _uiState.update { prev ->
            val sections = (prev.feed?.groups ?: emptyList()).toMutableList()
            sections.removeAt(sectionIndex)
            prev.copy(feed = prev.feed?.copy(groups = sections))
        }
    }

    fun onSectionsReordered(sections: List<OpdsGroup>) {
        _uiState.update { prev ->
            prev.copy(feed = prev.feed?.copy(groups = sections))
        }
    }

    fun onItemsReordered(sectionIndex: Int, items: List<Any>) {
        _uiState.update { prev ->
            val sections = (prev.feed?.groups ?: emptyList()).toMutableList()
            val section = sections.getOrNull(sectionIndex)
                ?: throw IllegalStateException("No section at index $sectionIndex")
            sections[sectionIndex] = if (section.navigation != null) {
                section.copy(navigation = items.filterIsInstance<ReadiumLink>())
            } else {
                section.copy(publications = items.filterIsInstance<OpdsPublication>())
            }
            prev.copy(feed = prev.feed?.copy(groups = sections))
        }
    }

    fun onClickDeleteItem(sectionIndex: Int, itemIndex: Int) {
        _uiState.update { prev ->
            val sections = (prev.feed?.groups ?: emptyList()).toMutableList()
            val section = sections.getOrNull(sectionIndex)
                ?: throw IllegalStateException("No section at index $sectionIndex")
            sections[sectionIndex] = if (section.navigation != null) {
                val items = (section.navigation ?: emptyList()).toMutableList()
                items.removeAt(itemIndex)
                section.copy(navigation = items)
            } else {
                val items = (section.publications ?: emptyList()).toMutableList()
                items.removeAt(itemIndex)
                section.copy(publications = items)
            }
            prev.copy(feed = prev.feed?.copy(groups = sections))
        }
    }

    fun onClickMoveItem(sectionIndex: Int, itemIndex: Int) {
        val sections = _uiState.value.feed?.groups ?: emptyList()
        val fromSection = sections.getOrNull(sectionIndex)
            ?: throw IllegalStateException("No section at index $sectionIndex")

        val compatibleSections = sections.mapIndexedNotNull { index, section ->
            if (index == sectionIndex) return@mapIndexedNotNull null
            val isCompatible = if (fromSection.navigation != null) {
                section.navigation != null
            } else {
                section.publications != null
            }
            if (!isCompatible) return@mapIndexedNotNull null
            MovingItemState.CompatibleSection(
                sectionIndex = index,
                title = section.metadata.title,
                itemCount = (section.navigation?.size ?: 0) + (section.publications?.size ?: 0),
            )
        }

        if (compatibleSections.size == 1) {
            moveItemToSection(sectionIndex, itemIndex, compatibleSections.first().sectionIndex)
        } else {
            _uiState.update {
                it.copy(
                    movingItem = MovingItemState(
                        fromSectionIndex = sectionIndex,
                        itemIndex = itemIndex,
                        compatibleSections = compatibleSections,
                    )
                )
            }
        }
    }

    fun onClickMoveItemToSection(targetSectionIndex: Int) {
        val moving = _uiState.value.movingItem
            ?: throw IllegalStateException(
                "onClickMoveItemToSection called but no item is being moved"
            )
        _uiState.update { it.copy(movingItem = null) }
        moveItemToSection(moving.fromSectionIndex, moving.itemIndex, targetSectionIndex)
    }

    fun onDismissMoveDialog() {
        _uiState.update { it.copy(movingItem = null) }
    }

    private fun moveItemToSection(sectionIndex: Int, itemIndex: Int, targetSectionIndex: Int) {
        _uiState.update { prev ->
            val sections = (prev.feed?.groups ?: emptyList()).toMutableList()
            val fromSection = sections.getOrNull(sectionIndex)
                ?: throw IllegalStateException("No section at index $sectionIndex")
            val toSection = sections.getOrNull(targetSectionIndex)
                ?: throw IllegalStateException("No section at index $targetSectionIndex")

            sections[sectionIndex] = if (fromSection.navigation != null) {
                val items = (fromSection.navigation ?: emptyList()).toMutableList()
                val item = items.removeAt(itemIndex)
                sections[targetSectionIndex] = toSection.copy(
                    navigation = (toSection.navigation ?: emptyList()) + item
                )
                fromSection.copy(navigation = items)
            } else {
                val items = (fromSection.publications ?: emptyList()).toMutableList()
                val item = items.removeAt(itemIndex)
                sections[targetSectionIndex] = toSection.copy(
                    publications = (toSection.publications ?: emptyList()) + item
                )
                fromSection.copy(publications = items)
            }
            prev.copy(feed = prev.feed?.copy(groups = sections))
        }
    }

    fun onClickAddItem(sectionIndex: Int) {
        pendingAddItemSectionIndex = sectionIndex
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                destination = RespectAppLauncher.create(
                    resultDest = RouteResultDest(
                        resultPopUpTo = route,
                        resultKey = KEY_LEARNING_UNIT,
                    )
                ),
            )
        )
    }

    fun onClickAddPlaylist(sectionIndex: Int) {
        pendingAddPlaylistSectionIndex = sectionIndex
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                destination = RespectAppLauncher.create(
                    resultDest = RouteResultDest(
                        resultPopUpTo = route,
                        resultKey = KEY_PLAYLIST,
                    )
                ),
            )
        )
    }

    fun onClickSave() {
        val feed = _uiState.value.feed ?: return
        if (feed.metadata.title.isBlank()) {
            _uiState.update { it.copy(titleError = true) }
            return
        }

        viewModelScope.launch {
            val activeAccount = accountManager.activeAccount
                ?: throw IllegalStateException("No active account when saving playlist")

            val playlistListUrl = Url(
                "${activeAccount.school.self}${OpdsFeedDataSource.PLAYLIST_ENDPOINT_NAME}"
            )

            val selfHref = feed.selfUrl()?.toString()
                ?: throw IllegalStateException("Playlist has no self URL")

            val sectionCount = feed.groups?.size ?: 0
            val itemCount = feed.groups?.sumOf { group ->
                (group.publications?.size ?: 0) + (group.navigation?.size ?: 0)
            } ?: 0

            val existingListFeed = schoolDataSource.opdsFeedDataSource
                .getByUrl(url = playlistListUrl, params = DataLoadParams())
                .dataOrNull()

            val playlistAsPublication = OpdsPublication(
                metadata = ReadiumMetadata(
                    title = LangMapStringValue(feed.metadata.title),
                    description = feed.metadata.description,
                    modified = feed.metadata.modified?.toString(),
                    numberOfPages = sectionCount,
                    duration = itemCount.toDouble(),
                ),
                links = feed.links,
            )

            val updatedPublications = if (existingListFeed != null) {
                val others = (existingListFeed.publications ?: emptyList()).filter { pub ->
                    pub.links.none { link ->
                        link.rel?.contains(PlaylistListUiState.REL_SELF) == true &&
                                link.href == selfHref
                    }
                }
                others + playlistAsPublication
            } else {
                listOf(playlistAsPublication)
            }

            val listModified = Instant.fromEpochMilliseconds(systemTimeInMillis())

            val listFeed = OpdsFeed(
                metadata = OpdsFeedMetadata(
                    title = existingListFeed?.metadata?.title ?: "",
                    modified = listModified,
                ),
                links = existingListFeed?.links ?: listOf(
                    ReadiumLink(
                        href = playlistListUrl.toString(),
                        rel = listOf(PlaylistListUiState.REL_SELF),
                        type = OpdsFeed.MEDIA_TYPE,
                    )
                ),
                publications = updatedPublications,
                groups = existingListFeed?.groups ?: emptyList(),
            )

            schoolDataSource.opdsFeedDataSource.store(listOf(feed, listFeed))

            val savedPlaylistUrl = feed.selfUrl()
                ?: throw IllegalStateException("Saved playlist has no self URL")

            _navCommandFlow.tryEmit(
                NavCommand.Navigate(
                    destination = PlaylistDetail.create(playlistUrl = savedPlaylistUrl),
                    popUpTo = PlaylistList.create(),
                    popUpToInclusive = false,
                )
            )
        }
    }

    companion object {
        const val KEY_LEARNING_UNIT = "result_learning_unit"
        const val KEY_PLAYLIST = "result_playlist"
        private const val KEY_PENDING_ADD_ITEM_SECTION_INDEX = "pending_add_item_section_index"
        private const val KEY_PENDING_ADD_PLAYLIST_SECTION_INDEX =
            "pending_add_playlist_section_index"
    }
}