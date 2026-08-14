package world.respect.shared.viewmodel.learningunit.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import io.github.aakira.napier.Napier
import io.ktor.http.Url
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.db.school.ext.isAdmin
import world.respect.datalayer.school.domain.MakePlaylistOpdsFeedUseCase
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.DataLoadingState
import world.respect.lib.dataloadstate.DataReadyState
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.lib.dataloadstate.ext.map
import world.respect.lib.opds.model.LangMapStringValue
import world.respect.lib.opds.model.OpdsFacet
import world.respect.lib.opds.model.OpdsFeed
import world.respect.lib.opds.model.OpdsPublication
import world.respect.lib.opds.model.ReadiumLink
import world.respect.lib.opds.model.ext.allPublications
import world.respect.lib.opds.model.findSelfLinks
import world.respect.libutil.ext.resolve
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.openexternallink.OpenExternalLinkUseCase
import world.respect.shared.ext.resultExpected
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.edit
import world.respect.shared.generated.resources.language
import world.respect.shared.generated.resources.not_found
import world.respect.shared.navigation.AssignmentEdit
import world.respect.shared.navigation.LearningUnitDetail
import world.respect.shared.navigation.OpdsFeedDetail
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.NavResultReturner
import world.respect.shared.navigation.PlaylistEdit
import world.respect.shared.navigation.PlaylistShare
import world.respect.shared.navigation.sendResultIfResultExpected
import world.respect.shared.util.SortOrderOption
import world.respect.shared.util.exception.getUiTextOrGeneric
import world.respect.shared.util.ext.asUiText
import world.respect.shared.util.ext.resolve
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.FabUiState
import world.respect.shared.viewmodel.app.appstate.Snack
import world.respect.shared.viewmodel.app.appstate.SnackBarDispatcher
import world.respect.shared.viewmodel.assignment.edit.AssignmentEditViewModel
import world.respect.shared.viewmodel.learningunit.OpdsPublicationsSelection
import world.respect.shared.viewmodel.learningunit.editfeed.PlaylistEditViewModel

data class OpdsFeedDetailUiState(
    val feed: DataLoadState<OpdsFeed> = DataLoadingState(),
    val facetOptions: List<OpdsFacet> = emptyList(),
    val selectedFilterTitle: String? = null,
    val sortOptions: List<SortOrderOption> = emptyList(),
    val activeSortOrderOption: SortOrderOption = SortOrderOption(
        Res.string.language, 1, true
    ),
    val fieldsEnabled: Boolean = true,
    val isTeacherOrAdmin: Boolean = false,
    val collapsedSections: Set<String> = emptySet(),
    val isMultiSelectMode: Boolean = false,
    val selectedPublications: Set<String> = emptySet(),
    val showCopyDialog: Boolean = false,
    val copyDialogName: String = "",
    val showDeleteDialog: Boolean = false,
    val showSelectPlaylistButton: Boolean = false,
    val selectedNavigation: ReadiumLink? = null,
) {
    fun isSectionCollapsed(sectionKey: String) = sectionKey in collapsedSections

    fun isPublicationSelected(publication: OpdsPublication): Boolean =
        publication.metadata.identifier?.toString() in selectedPublications

    fun isNavigationSelected(navigation: ReadiumLink): Boolean =
        navigation.href == selectedNavigation?.href

    val selectedCount: Int
        get() = selectedPublications.size

    val showAssignButton: Boolean
        get() = isTeacherOrAdmin && !feed.dataOrNull()?.allPublications().isNullOrEmpty()
}

/**
 * Show a list of learning units as provided by an OpdsFeed
 */
class OpdsFeedDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val accountManager: RespectAccountManager,
    private val resultReturner: NavResultReturner,
    private val openExternalLinkUseCase: OpenExternalLinkUseCase,
    private val snackBarDispatcher: SnackBarDispatcher,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()

    private val schoolDataSource: SchoolDataSource by inject()

    private val makePlaylistOpdsFeedUseCase: MakePlaylistOpdsFeedUseCase by inject()

    private val _uiState = MutableStateFlow(OpdsFeedDetailUiState())

    val uiState = _uiState.asStateFlow()

    private val route: OpdsFeedDetail = savedStateHandle.toRoute()

    init {
        _uiState.update {
            it.copy(
                showSelectPlaylistButton =
                    route.resultDest?.resultKey == PlaylistEditViewModel.KEY_PLAYLIST
            )
        }

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
                url = route.opdsFeedUrl,
                params = DataLoadParams()
            ).collect { result ->
                _uiState.update { prev ->
                    prev.copy(
                        feed = result.map { it.resolve(route.opdsFeedUrl) }
                    )
                }

                if(result is DataReadyState) {
                    _appUiState.update { it.copy(title = result.data.metadata.title.asUiText()) }
                }
            }
        }
    }

    fun onSortOrderChanged(sortOption: SortOrderOption) {
        _uiState.update { it.copy(activeSortOrderOption = sortOption) }
    }

    fun onClickPublication(publication: OpdsPublication) {
        when {
            uiState.value.isMultiSelectMode -> {

            }

            route.resultExpected -> {
                resultReturner.sendResultIfResultExpected(
                    route = route,
                    navCommandFlow = _navCommandFlow,
                    result = OpdsPublicationsSelection(
                        url = route.opdsFeedUrl,
                        selectedPublications = listOf(publication),
                    )
                )
            }

            else -> {
                val manifestUrl = publication.findSelfLinks().firstOrNull()?.href?.let {
                    route.opdsFeedUrl.resolve(it)
                } ?: return run {
                    snackBarDispatcher.showSnackBar(Snack(Res.string.not_found.asUiText()))
                }

                //Needs to handle external link.
                _navCommandFlow.tryEmit(
                    value = NavCommand.Navigate(
                        LearningUnitDetail.create(
                            learningUnitManifestUrl = manifestUrl,
                            refererUrl = route.opdsFeedUrl,
                            expectedIdentifier = publication.metadata.identifier?.toString(),
                            title = uiState.value.feed.dataOrNull()?.metadata?.title?.let {
                                LangMapStringValue(it)
                            }
                        )
                    )
                )
            }
        }
    }

    fun onLongPressPublication(publication: OpdsPublication) {
        _uiState.update { it.copy(isMultiSelectMode = true) }
        toggleSelection(publication)
    }

    private fun toggleSelection(publication: OpdsPublication) {
        val id = publication.metadata.identifier?.toString()
            ?: throw IllegalStateException(
                "Publication has no identifier: ${publication.metadata.title}"
            )
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

        /*
        val allPublications = currentState.publications +
                currentState.group.flatMap { it.publications ?: emptyList() }

        val selections = allPublications
            .filter { pub ->
                pub.metadata.identifier?.toString() in currentState.selectedPublications
            }
            .map { publication ->
                LearningUnitSelection(
                    learningUnitManifestUrl = resolvePublicationManifestUrl(publication),
                    selectedPublication = publication,
                )
            }

        resultReturner.sendResultIfResultExpected(
            route = route,
            navCommandFlow = _navCommandFlow,
            result = selections,
        )
         */
    }

    fun onClickNavigation(navigation: ReadiumLink) {
        val resolvedUrl = route.opdsFeedUrl.resolve(navigation.href)

        if (route.resultDest?.resultKey == PlaylistEditViewModel.KEY_PLAYLIST) {
            _uiState.update { prev ->
                val isDeselecting = prev.selectedNavigation?.href == resolvedUrl.toString()
                prev.copy(
                    isMultiSelectMode = !isDeselecting,
                    selectedNavigation = if (isDeselecting) {
                        null
                    } else {
                        navigation.copy(href = resolvedUrl.toString())
                    }
                )
            }
            return
        }

        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                OpdsFeedDetail.create(
                    opdsFeedUrl = resolvedUrl,
                    resultDest = route.resultDest,
                )
            )
        )
    }

    fun onClickSelectPlaylist() {
        resultReturner.sendResultIfResultExpected(
            route = route,
            navCommandFlow = _navCommandFlow,
            result = _uiState.value.selectedNavigation ?: return
        )
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
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(PlaylistShare.create(playlistUrl = route.opdsFeedUrl))
        )
    }

    fun onClickCopy() {
        /*
        val feed = _uiState.value.feed
            ?: throw IllegalStateException("onClickCopy called but feed is null")
        _uiState.update {
            it.copy(
                showCopyDialog = true,
                copyDialogName = feed.metadata.title,
            )
        }*/
    }

    fun onCopyDialogDismiss() {
        _uiState.update { it.copy(showCopyDialog = false, copyDialogName = "") }
    }

    fun onCopyDialogNameChanged(name: String) {
        _uiState.update { it.copy(copyDialogName = name) }
    }

    fun onCopyDialogConfirm() {
        /*
        val feed = _uiState.value.feed
            ?: throw IllegalStateException("onCopyDialogConfirm called but feed is null")
        val newName = _uiState.value.copyDialogName.trim()
        if (newName.isBlank())
            throw IllegalStateException("onCopyDialogConfirm called but the new name is blank")

        viewModelScope.launch {
            val sessionAndPerson = accountManager.selectedAccountAndPersonFlow.first()
                ?: throw IllegalStateException("No active session when copying feed")

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
        }*/
    }

    fun onClickDelete() {
        _uiState.update { it.copy(showDeleteDialog = true) }
    }

    fun onDeleteDialogDismiss() {
        _uiState.update { it.copy(showDeleteDialog = false) }
    }

    fun onDeleteDialogConfirm() {
        /*
        viewModelScope.launch {
            val feed = _uiState.value.feed
                ?: throw IllegalStateException("onDeleteDialogConfirm called but feed is null")
            val selfUrl = feed.selfUrl()
                ?: throw IllegalStateException("Cannot delete: feed has no self URL")

            schoolDataSource.opdsFeedDataSource.deleteByUrl(selfUrl)

            _uiState.update { it.copy(showDeleteDialog = false) }
            _navCommandFlow.tryEmit(
                NavCommand.Navigate(
                    destination = RespectAppLauncher.create()
                )
            )
        }*/
    }

    fun onClickAssignQuickActionButton() {
        try {
            _navCommandFlow.tryEmit(
                NavCommand.Navigate(
                    AssignmentEdit.create(
                        assignmentActivityId = null,
                        learningUnitSelected = OpdsPublicationsSelection(
                            url = route.opdsFeedUrl,
                            selectedPublications = uiState.value.feed.dataOrNull()?.let { feed ->
                                feed.publications.orEmpty() + feed.groups.orEmpty().flatMap {
                                    it.publications.orEmpty()
                                }
                            }.orEmpty().take(AssignmentEditViewModel.MAX_UNITS_TO_ADD)
                        )
                    )
                )
            )
        }catch(e: Throwable) {
            Napier.w("Exception on click assign items", e)
            snackBarDispatcher.showSnackBar(Snack(e.getUiTextOrGeneric()))
        }
    }

    fun onClickAssignSection(sectionIndex: Int) {
        /*
        val feed = _uiState.value.feed
            ?: throw IllegalStateException("Cannot assign: no feed loaded")
        val feedUrl = feed.selfUrl()
            ?: throw IllegalStateException("Cannot assign: feed has no self URL")

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

        val learningUnitManifestUrl = feedUrl.resolve(
            publicationSelfLink(firstPublication).href
        )

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

         */
    }


    fun onClickEdit() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(PlaylistEdit.create(playlistUrl = route.opdsFeedUrl))
        )
    }

    private fun publicationSelfLink(publication: OpdsPublication): ReadiumLink {
        return publication.links.find {
            it.rel?.contains(SELF) == true
        } ?: throw IllegalStateException(
            "Publication has no self link: ${publication.metadata.title}"
        )
    }

    private fun resolvePublicationManifestUrl(publication: OpdsPublication): Url {
        return route.opdsFeedUrl.resolve(publicationSelfLink(publication).href)
    }

    companion object {
        const val SELF = "self"
        const val ICON = "icon"
        const val ASSIGN_HEADER_SECTION_INDEX = -1
        private const val MIME_TYPE_HTML = "text/html"
    }
}