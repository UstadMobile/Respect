package world.respect.shared.viewmodel.learningunit.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
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
import world.respect.datalayer.school.domain.MakePlaylistOpdsFeedUseCase
import world.respect.datalayer.school.opds.ext.selfUrl
import world.respect.lib.opds.model.OpdsFacet
import world.respect.lib.opds.model.OpdsFeed
import world.respect.lib.opds.model.OpdsGroup
import world.respect.lib.opds.model.OpdsPublication
import world.respect.lib.opds.model.ReadiumLink
import world.respect.libutil.ext.resolve
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.edit
import world.respect.shared.generated.resources.language
import world.respect.shared.navigation.LearningUnitDetail
import world.respect.shared.navigation.LearningUnitList
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.NavResultReturner
import world.respect.shared.navigation.PlaylistDetail
import world.respect.shared.navigation.PlaylistEdit
import world.respect.shared.navigation.sendResultIfResultExpected
import world.respect.shared.util.SortOrderOption
import world.respect.shared.util.ext.asUiText
import world.respect.shared.util.ext.resolve
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.AppBarSearchUiState
import world.respect.shared.viewmodel.app.appstate.FabUiState
import world.respect.shared.viewmodel.learningunit.LearningUnitSelection
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
) {
    fun isSectionCollapsed(sectionTitle: String) = sectionTitle in collapsedSections

    fun isPublicationSelected(publication: OpdsPublication): Boolean =
        publication.metadata.identifier?.toString() in selectedPublications

    val selectedCount: Int
        get() = selectedPublications.size

    val hasLearningItems: Boolean
        get() = publications.isNotEmpty() ||
                group.any { it.publications?.isNotEmpty() == true }
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
        viewModelScope.launch {
            _appUiState.update {
                it.copy(
                    searchState = AppBarSearchUiState(visible = true)
                )
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
        _uiState.update {
            it.copy(activeSortOrderOption = sortOption)
        }
    }

    fun onClickPublication(publication: OpdsPublication) {
        val isPickMode = route.resultDest != null

        if (isPickMode || _uiState.value.isMultiSelectMode) {
            _uiState.update { it.copy(isMultiSelectMode = true) }
            toggleSelection(publication)
            return
        }

        val publicationHref = publication.links.find {
            it.rel?.contains(SELF) == true
        }?.href.toString()

        val refererUrl = route.opdsFeedUrl.resolve(publicationHref).toString()
        val learningUnitManifestUrl = route.opdsFeedUrl.resolve(publicationHref)

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
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                LearningUnitList.create(
                    opdsFeedUrl = route.opdsFeedUrl.resolve(navigationHref),
                    appManifestUrl = route.appManifestUrl,
                    resultDest = route.resultDest,
                )
            )
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
                        _uiState.update {
                            it.withFeedContent(result.data)
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    fun onClickToggleSection(sectionTitle: String) {
        _uiState.update { prev ->
            val updatedCollapsed = if (sectionTitle in prev.collapsedSections) {
                prev.collapsedSections - sectionTitle
            } else {
                prev.collapsedSections + sectionTitle
            }
            prev.copy(collapsedSections = updatedCollapsed)
        }
    }

    fun onClickShare() {
        // TODO: implement share playlist
    }

    fun onClickCopyPlaylist() {
        val feed = _uiState.value.feed ?: return
        _uiState.update {
            it.copy(
                showCopyDialog = true,
                copyDialogName = "Copy the ${feed.metadata.title}",
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

        val activeAccount = accountManager.activeAccount
            ?: throw IllegalStateException(
                "No active account when copying playlist"
            )

        viewModelScope.launch {
            @OptIn(ExperimentalUuidApi::class)
            val copiedFeed = MakePlaylistOpdsFeedUseCase(
                schoolUrl = activeAccount.school.self
            ).invoke(
                base = feed.copy(
                    metadata = feed.metadata.copy(title = newName)
                ),
                userGuid = activeAccount.userGuid,
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
        // TODO: implement delete playlist
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
}