package world.respect.shared.viewmodel.catalog.opdsfeedlist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.db.school.ext.isAdmin
import world.respect.datalayer.school.domain.MakePlaylistOpdsFeedUseCase
import world.respect.datalayer.school.opds.ext.requireSelfUrl
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.DataLoadingState
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.lib.opds.model.OpdsFeed
import world.respect.lib.xapi.OpenEelXapiConstants
import world.respect.lib.xapi.ext.distinctByMostRecentTimestampForActivityId
import world.respect.lib.xapi.model.XapiStatement
import world.respect.lib.xapi.model.XapiVerb
import world.respect.lib.xapi.resources.XapiStatementsResource
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.catalog.getopdsfeedforxapiactivity.GetOpdsFeedForXapiActivityUseCase
import world.respect.shared.ext.resultExpected
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.add_from_a_link
import world.respect.shared.generated.resources.add_new
import world.respect.shared.generated.resources.home
import world.respect.shared.generated.resources.collection
import world.respect.shared.navigation.EnterLink
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.OpdsFeedDetail
import world.respect.shared.navigation.OpdsFeedEdit
import world.respect.shared.navigation.PlaylistList
import world.respect.shared.util.ext.appbarTitleString
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.ExpandableFabIcon
import world.respect.shared.viewmodel.app.appstate.ExpandableFabItem
import world.respect.shared.viewmodel.app.appstate.ExpandableFabUiState

enum class OpdsFeedListFilter {
    ALL,
    MY_PLAYLISTS,
}

data class OpdsFeedListUiState(
    val statements: List<XapiStatement> = emptyList(),
    val feedFlowForActivity: (XapiStatement) -> Flow<DataLoadState<OpdsFeed>> = {
        flowOf(DataLoadingState())
    },
    val activeFilter: OpdsFeedListFilter = OpdsFeedListFilter.ALL,
    val isTeacherOrAdmin: Boolean = false,
    val activeUserOwnerHref: String = "",
    val activeUsername: String = "",
) {

    /**
     * Whether a resolved OpdsFeed (collection) should be shown given the current activeFilter.
     */
    fun matchesActiveFilter(feed: OpdsFeed): Boolean {
        return when (activeFilter) {
            OpdsFeedListFilter.ALL -> true
            OpdsFeedListFilter.MY_PLAYLISTS -> feed.links.any { link ->
                link.rel?.contains(MakePlaylistOpdsFeedUseCase.REL_OWNER) == true
                        && link.href == activeUserOwnerHref
            }
        }
    }

}

class OpdsFeedListViewModel(
    savedStateHandle: SavedStateHandle,
    private val accountManager: RespectAccountManager,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()

    private val schoolDataSource: SchoolDataSource by inject()

    private val getOpdsFeedForXapiActivityUseCase: GetOpdsFeedForXapiActivityUseCase by inject()

    private val _uiState = MutableStateFlow(OpdsFeedListUiState())

    val uiState = _uiState.asStateFlow()

    private val route: PlaylistList = savedStateHandle.toRoute()

    init {
        _appUiState.update {
            it.copy(
                title = route.opdsPickType?.appbarTitleString?.asUiText() ?: Res.string.home.asUiText(),
                hideBottomNavigation = route.resultExpected,
            )
        }

        _uiState.update {
            it.copy(feedFlowForActivity = getOpdsFeedForXapiActivityUseCase::invoke)
        }

        viewModelScope.launch {
            accountManager.selectedAccountAndPersonFlow.collect { sessionAndPerson ->
                val isTeacherOrAdmin = sessionAndPerson?.person?.isAdmin() == true

                val username = sessionAndPerson?.person?.username.orEmpty()
                val activeUserOwnerHref = sessionAndPerson?.let {
                    MakePlaylistOpdsFeedUseCase.getUserProfileUrl(
                        schoolUrl = it.session.account.school.self,
                        username = username,
                    )
                }.orEmpty()

                _uiState.update {
                    it.copy(
                        isTeacherOrAdmin = isTeacherOrAdmin,
                        activeUserOwnerHref = activeUserOwnerHref,
                        activeUsername = username,
                    )
                }

                _appUiState.update {
                    it.copy(
                        title = Res.string.home.asUiText(),
                        expandableFabState = ExpandableFabUiState(
                            visible = isTeacherOrAdmin && !route.resultExpected,
                            text = Res.string.collection.asUiText(),
                            items = listOf(
                                ExpandableFabItem(
                                    icon = ExpandableFabIcon.ADD,
                                    text = Res.string.add_new.asUiText(),
                                    onClick = ::onClickAddNew,
                                ),
                                ExpandableFabItem(
                                    icon = ExpandableFabIcon.LINK,
                                    text = Res.string.add_from_a_link.asUiText(),
                                    onClick = ::onClickAddFromLink,
                                ),
                            )
                        )
                    )
                }
            }
        }

        viewModelScope.launch {
            /*
             * As per the Collections Listing recipe (README_COLLECTIONS_LISTING_RECIPE.md), pinned
             * collections are retrieved using a get statements query filtered by the pin-collection
             * verb and collection-listing recipe category, with related activities included.
             *
             * Saving (creating or editing) a collection always posts a new pin-collection statement
             * for the same collection activity id (statements are immutable, so editing cannot
             * modify a previously posted statement). Therefor the statements are reduced to only
             * the most recent statement per collection activity id, so that editing a collection
             * updates its entry in the list instead of appearing as an additional/new collection.
             */
            schoolDataSource.xapiResource.statements.getAsFlow(
                listParams = XapiStatementsResource.GetStatementParams(
                    verb = XapiVerb.ID_PIN_COLLECTION,
                    activity = OpenEelXapiConstants.CATEGORY_COLLECTION_LISTING_RECIPE,
                    relatedActivities = true,
                ),
                dataLoadParams = DataLoadParams(),
            ).collect { state ->
                _uiState.update {
                    it.copy(
                        statements = state.dataOrNull()?.statements
                            ?.filter { statement -> statement.id != null }
                            ?.distinctByMostRecentTimestampForActivityId()
                            ?: emptyList()
                    )
                }
            }
        }
    }

    fun onClickFilter(filter: OpdsFeedListFilter) {
        _uiState.update { it.copy(activeFilter = filter) }
    }

    fun onClickPlaylist(feed: OpdsFeed) {
        val playlistUrl = feed.requireSelfUrl()

        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                OpdsFeedDetail.create(
                    opdsFeedUrl = playlistUrl,
                    resultDest = route.resultDest,
                    opdsPickType = route.opdsPickType,
                )
            )
        )
    }

    fun onClickAddNew() {
        _navCommandFlow.tryEmit(NavCommand.Navigate(OpdsFeedEdit.create()))
    }

    fun onClickAddFromLink() {
        _navCommandFlow.tryEmit(NavCommand.Navigate(EnterLink))
    }
}