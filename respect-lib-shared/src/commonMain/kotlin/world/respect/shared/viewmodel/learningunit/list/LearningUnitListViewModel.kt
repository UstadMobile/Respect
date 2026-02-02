package world.respect.shared.viewmodel.learningunit.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.ustadmobile.libcache.PublicationPinState
import com.ustadmobile.libcache.UstadCache
import io.ktor.http.Url
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
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
import world.respect.datalayer.RespectAppDataSource
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.ext.dataOrNull
import world.respect.datalayer.school.SchoolAppDataSource
import world.respect.lib.opds.model.OpdsFacet
import world.respect.lib.opds.model.OpdsGroup
import world.respect.lib.opds.model.OpdsPublication
import world.respect.lib.opds.model.ReadiumLink
import world.respect.libutil.ext.resolve
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.downloaded_lessons
import world.respect.shared.generated.resources.item_deleted
import world.respect.shared.generated.resources.language
import world.respect.shared.generated.resources.sort_by
import world.respect.shared.navigation.LearningUnitDetail
import world.respect.shared.navigation.LearningUnitList
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.NavResultReturner
import world.respect.shared.navigation.sendResultIfResultExpected
import world.respect.shared.util.SortOrderOption
import world.respect.shared.util.ext.asUiText
import world.respect.shared.util.ext.resolve
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.AppBarSearchUiState
import world.respect.shared.viewmodel.app.appstate.Snack
import world.respect.shared.viewmodel.app.appstate.SnackBarDispatcher
import world.respect.shared.viewmodel.learningunit.LearningUnitSelection

data class LearningUnitListUiState(
    val publications: List<OpdsPublication> = emptyList(),
    val downloadedLessons: List<LearningUnitSelection> = emptyList(),
    val navigation: List<ReadiumLink> = emptyList(),
    val group: List<OpdsGroup> = emptyList(),
    val facetOptions: List<OpdsFacet> = emptyList(),
    val selectedFilterTitle: String? = null,
    val sortOptions: List<SortOrderOption> = emptyList(),
    val activeSortOrderOption: SortOrderOption = SortOrderOption(
        Res.string.language, 1, true
    ),
    val fieldsEnabled: Boolean = true,
    val showOnlyDownloaded: Boolean = false,
    val showDeleteButton: Boolean = false,
    val isLoadingDownloaded: Boolean = false
)

class LearningUnitListViewModel(
    savedStateHandle: SavedStateHandle,
    private val appDataSource: RespectAppDataSource,
    private val resultReturner: NavResultReturner,
    private val ustadCache: UstadCache,
    private val snackBarDispatcher: SnackBarDispatcher,
    accountManager: RespectAccountManager,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()
    private val schoolDataSource: SchoolDataSource by inject()

    private val _uiState = MutableStateFlow(LearningUnitListUiState())
    val uiState = _uiState.asStateFlow()
    private val route: LearningUnitList = savedStateHandle.toRoute()

    init {
        viewModelScope.launch {
            _appUiState.update {
                it.copy(
                    searchState = AppBarSearchUiState(visible = !route.showOnlyDownloaded),
                    title = if (route.showOnlyDownloaded) {
                        Res.string.downloaded_lessons.asUiText()
                    } else {
                        it.title
                    },
                    showBackButton = true
                )
            }

            if (route.showOnlyDownloaded) {
                loadAllDownloadedLessons()
            } else {
                loadFromSingleFeed()
            }
        }
    }

    private suspend fun loadFromSingleFeed() {
        appDataSource.opdsDataSource.loadOpdsFeed(
            url = route.opdsFeedUrl,
            params = DataLoadParams()
        ).collect { result ->
            when (result) {
                is DataReadyState -> {
                    val resolvedFeed = result.data.resolve(route.opdsFeedUrl)
                    val appBarTitle = result.data.metadata.title
                    val facetOptions = result.data.facets ?: emptyList()
                    val sortOptions = facetOptions.mapIndexed { index, facet ->
                        SortOrderOption(
                            fieldMessageId = Res.string.language,
                            flag = index + 1,
                            order = true
                        )
                    }

                    _appUiState.update {
                        it.copy(
                            title = appBarTitle.asUiText(),
                            searchState = AppBarSearchUiState(visible = true)
                        )
                    }

                    _uiState.update {
                        it.copy(
                            navigation = resolvedFeed.navigation ?: emptyList(),
                            publications = resolvedFeed.publications ?: emptyList(),
                            group = resolvedFeed.groups ?: emptyList(),
                            facetOptions = facetOptions,
                            sortOptions = sortOptions,
                            showOnlyDownloaded = false,
                            showDeleteButton = false,
                            downloadedLessons = emptyList()
                        )
                    }
                }
                else -> {}
            }
        }
    }

    private suspend fun loadAllDownloadedLessons() {
        try {
            _uiState.update {
                it.copy(isLoadingDownloaded = true)
            }

            val sortOptions = createDownloadedLessonsSortOptions()

            _uiState.update {
                it.copy(
                    downloadedLessons = emptyList(),
                    publications = emptyList(),
                    navigation = emptyList(),
                    group = emptyList(),
                    showOnlyDownloaded = true,
                    showDeleteButton = true,
                    sortOptions = sortOptions,
                    activeSortOrderOption = sortOptions.first(),
                    facetOptions = emptyList()
                )
            }

            val downloadedLessons = mutableListOf<LearningUnitSelection>()
            val seenManifestUrls = mutableSetOf<String>()
            val lock = Any()

            val schoolAppsResult = schoolDataSource.schoolAppDataSource.list(
                loadParams = DataLoadParams(),
                params = SchoolAppDataSource.GetListParams()
            )

            val allApps = schoolAppsResult.dataOrNull()

            if (allApps == null) {
                _uiState.update { it.copy(isLoadingDownloaded = false) }
                return
            }

            if (allApps.isEmpty()) {
                _uiState.update { it.copy(isLoadingDownloaded = false) }
                return
            }

            allApps.map { app ->
                viewModelScope.async {
                    try {
                        processAppForDownloadedLessons(
                            app = app,
                            downloadedLessons = downloadedLessons,
                            seenManifestUrls = seenManifestUrls,
                            lock = lock
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }.awaitAll()

            _uiState.update {
                it.copy(
                    downloadedLessons = downloadedLessons.toList(),
                    isLoadingDownloaded = false
                )
            }

        } catch (e: Exception) {
            e.printStackTrace()
            _uiState.update { it.copy(isLoadingDownloaded = false) }
        }
    }

    private fun createDownloadedLessonsSortOptions(): List<SortOrderOption> {
        return listOf(
            SortOrderOption(
                fieldMessageId = Res.string.sort_by,
                flag = 0,
                order = true
            ),
            SortOrderOption(
                fieldMessageId = Res.string.language,
                flag = 1,
                order = true
            )
        )
    }

    private suspend fun processAppForDownloadedLessons(
        app: world.respect.datalayer.school.model.SchoolApp,
        downloadedLessons: MutableList<LearningUnitSelection>,
        seenManifestUrls: MutableSet<String>,
        lock: Any
    ) {
        val appManifest = appDataSource.compatibleAppsDataSource.getAppAsFlow(
            manifestUrl = app.appManifestUrl,
            loadParams = DataLoadParams()
        ).first().dataOrNull() ?: return

        val learningUnitsUrl = app.appManifestUrl.resolve(
            appManifest.learningUnits.toString()
        )

        val mainFeed = appDataSource.opdsDataSource.loadOpdsFeed(
            url = learningUnitsUrl,
            params = DataLoadParams()
        ).first().dataOrNull() ?: return

        val resolvedFeed = mainFeed.resolve(learningUnitsUrl)

        resolvedFeed.navigation?.map { navLink ->
            viewModelScope.async {
                try {
                    processNavigationLinkForDownload(
                        navLink = navLink,
                        baseUrl = learningUnitsUrl,
                        appManifestUrl = app.appManifestUrl,
                        downloadedLessons = downloadedLessons,
                        seenManifestUrls = seenManifestUrls,
                        lock = lock
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }?.awaitAll()

        resolvedFeed.publications?.forEach { pub ->
            processPublicationForDownload(
                publication = pub,
                baseUrl = learningUnitsUrl,
                appManifestUrl = app.appManifestUrl,
                downloadedLessons = downloadedLessons,
                seenManifestUrls = seenManifestUrls,
                lock = lock
            )
        }
    }

    private suspend fun processNavigationLinkForDownload(
        navLink: ReadiumLink,
        baseUrl: Url,
        appManifestUrl: Url,
        downloadedLessons: MutableList<LearningUnitSelection>,
        seenManifestUrls: MutableSet<String>,
        lock: Any
    ) {
        val gradeFeedUrl = baseUrl.resolve(navLink.href)
        val gradeFeed = appDataSource.opdsDataSource.loadOpdsFeed(
            url = gradeFeedUrl,
            params = DataLoadParams()
        ).first().dataOrNull() ?: return

        val resolvedGradeFeed = gradeFeed.resolve(gradeFeedUrl)

        resolvedGradeFeed.publications?.forEach { pub ->
            processPublicationForDownload(
                publication = pub,
                baseUrl = gradeFeedUrl,
                appManifestUrl = appManifestUrl,
                downloadedLessons = downloadedLessons,
                seenManifestUrls = seenManifestUrls,
                lock = lock
            )
        }
    }

    private suspend fun processPublicationForDownload(
        publication: OpdsPublication,
        baseUrl: Url,
        appManifestUrl: Url,
        downloadedLessons: MutableList<LearningUnitSelection>,
        seenManifestUrls: MutableSet<String>,
        lock: Any
    ) {
        val selfLink = publication.links.find {
            it.rel?.contains(SELF) == true
        } ?: return

        val manifestUrl = baseUrl.resolve(selfLink.href)
        val manifestUrlStr = manifestUrl.toString()

        val isNew = synchronized(lock) {
            seenManifestUrls.add(manifestUrlStr)
        }

        if (!isNew) {
            return
        }

        val pinState = ustadCache.publicationPinState(manifestUrl).first()

        if (pinState.status == PublicationPinState.Status.READY) {
            val selection = LearningUnitSelection(
                learningUnitManifestUrl = manifestUrl,
                selectedPublication = publication,
                appManifestUrl = appManifestUrl
            )

            synchronized(lock) {
                downloadedLessons.add(selection)
            }

            _uiState.update {
                it.copy(downloadedLessons = synchronized(lock) {
                    downloadedLessons.toList()
                })
            }
        }
    }

    fun onClickDownloadedLesson(item: LearningUnitSelection) {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                LearningUnitDetail.create(
                    learningUnitManifestUrl = item.learningUnitManifestUrl,
                    appManifestUrl = item.appManifestUrl,
                    expectedIdentifier = item.selectedPublication.metadata.identifier.toString()
                )
            )
        )
    }

    fun onClickDeleteDownloaded(item: LearningUnitSelection) {
        val originalIndex = _uiState.value.downloadedLessons.indexOfFirst {
            it.learningUnitManifestUrl == item.learningUnitManifestUrl
        }

        _uiState.update {
            it.copy(
                downloadedLessons = it.downloadedLessons.filter { d ->
                    d.learningUnitManifestUrl != item.learningUnitManifestUrl
                }
            )
        }

        viewModelScope.launch {
            snackBarDispatcher.showSnackBar(
                Snack(
                    message = Res.string.item_deleted.asUiText(),
                    action = "Undo",
                    onAction = {
                        _uiState.update { s ->
                            val mutableList = s.downloadedLessons.toMutableList()
                            val insertIndex = originalIndex.coerceAtMost(mutableList.size)
                            mutableList.add(insertIndex, item)
                            s.copy(downloadedLessons = mutableList)
                        }
                    }
                )
            )

            delay(UNDO_TIMEOUT_MS)

            if (_uiState.value.downloadedLessons.none {
                    it.learningUnitManifestUrl == item.learningUnitManifestUrl
                }) {
                try {
                    ustadCache.unpinPublication(item.learningUnitManifestUrl)
                } catch (e: Exception) {
                    e.printStackTrace()
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
        const val UNDO_TIMEOUT_MS = 5000L
    }
}