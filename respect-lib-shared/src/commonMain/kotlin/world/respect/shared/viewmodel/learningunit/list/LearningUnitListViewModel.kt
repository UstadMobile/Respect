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
import world.respect.shared.navigation.LearningUnitDetail
import world.respect.shared.navigation.LearningUnitList
import world.respect.shared.viewmodel.app.appstate.AppBarSearchUiState
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataReadyState
import world.respect.datalayer.SchoolDataSource
import world.respect.lib.opds.model.OpdsFacet
import world.respect.lib.opds.model.OpdsFeed
import world.respect.lib.opds.model.OpdsGroup
import world.respect.lib.opds.model.OpdsPublication
import world.respect.lib.opds.model.ReadiumLink
import world.respect.libutil.ext.resolve
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.language
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.NavResultReturner
import world.respect.shared.navigation.sendResultIfResultExpected
import world.respect.shared.util.SortOrderOption
import world.respect.shared.util.ext.asUiText
import world.respect.shared.util.ext.resolve
import world.respect.shared.viewmodel.assignment.edit.AssignmentEditViewModel
import world.respect.shared.viewmodel.learningunit.LearningUnitSelection
import world.respect.shared.viewmodel.playlists.collections.edit.PlaylistEditViewModel

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
    val isMultiSelectMode: Boolean = false,
    val selectedPublications: Set<String> = emptySet(),
    val showSelectPlaylistButton: Boolean = false,
    val selectedNavigation: ReadiumLink? = null
) {
    fun isPublicationSelected(publication: OpdsPublication): Boolean =
        publication.metadata.identifier?.toString() in selectedPublications

    fun isNavigationSelected(navigation: ReadiumLink): Boolean =
        navigation.href == selectedNavigation?.href

    val selectedCount: Int
        get() = selectedPublications.size
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
                showSelectPlaylistButton =
                    route.resultDest?.resultKey == PlaylistEditViewModel.KEY_PLAYLIST
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
        if (route.resultDest != null &&
            route.resultDest.resultKey != PlaylistEditViewModel.KEY_PLAYLIST
        ) {
            if (route.resultDest.resultKey == AssignmentEditViewModel.KEY_LEARNING_UNIT) {
                val learningUnitManifestUrl = resolvePublicationManifestUrl(publication)
                resultReturner.sendResultIfResultExpected(
                    route = route,
                    navCommandFlow = _navCommandFlow,
                    result = LearningUnitSelection(
                        learningUnitManifestUrl = learningUnitManifestUrl,
                        selectedPublication = publication,
                    )
                )
                return
            }
            if (!_uiState.value.isMultiSelectMode) {
                _uiState.update { it.copy(isMultiSelectMode = true) }
            }
            toggleSelection(publication)
            return
        }
        if (route.resultDest?.resultKey == PlaylistEditViewModel.KEY_PLAYLIST) {
            val learningUnitManifestUrl = resolvePublicationManifestUrl(publication)
            _navCommandFlow.tryEmit(
                value = NavCommand.Navigate(
                    LearningUnitDetail.create(
                        learningUnitManifestUrl = learningUnitManifestUrl,
                        refererUrl = Url(learningUnitManifestUrl.toString()),
                        expectedIdentifier = publication.metadata.identifier.toString()
                    )
                )
            )
            return
        }

        if (_uiState.value.isMultiSelectMode) {
            toggleSelection(publication)
            return
        }

        val learningUnitManifestUrl = resolvePublicationManifestUrl(publication)

        if (
            !resultReturner.sendResultIfResultExpected(
                route = route,
                navCommandFlow = _navCommandFlow,
                result = LearningUnitSelection(
                    learningUnitManifestUrl = learningUnitManifestUrl,
                    selectedPublication = publication,
                )
            )
        ) {
            _navCommandFlow.tryEmit(
                value = NavCommand.Navigate(
                    LearningUnitDetail.create(
                        learningUnitManifestUrl = learningUnitManifestUrl,
                        refererUrl = route.opdsFeedUrl,
                        expectedIdentifier = publication.metadata.identifier.toString(),
                        title = publication.metadata.title,
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
        val id = publication.metadata.identifier?.toString()
            ?: throw IllegalStateException("Publication has no identifier: ${publication.metadata.title}")
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
    }

    fun onClickNavigation(navigation: ReadiumLink) {

        val navigationHref = navigation.href
        val resolvedUrl = route.opdsFeedUrl.resolve(navigationHref)

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
                LearningUnitList.create(
                    opdsFeedUrl = resolvedUrl,
                    appManifestUrl = route.appManifestUrl,
                    resultDest = route.resultDest,
                )
            )
        )
    }


    fun onClickSelectPlaylist() {
        resultReturner.sendResultIfResultExpected(
            route = route,
            navCommandFlow = _navCommandFlow,
            result =  _uiState.value.selectedNavigation ?: return
        )
    }
    private fun resolvePublicationManifestUrl(publication: OpdsPublication): Url {
        val publicationHref = publication.links.find {
            it.rel?.contains(SELF) == true
        }?.href ?: throw IllegalStateException(
            "Publication has no self link: ${publication.metadata.title}"
        )
        return route.opdsFeedUrl.resolve(publicationHref)
    }
    companion object {
        const val SELF = "self"
        const val ICON = "icon"
    }
}