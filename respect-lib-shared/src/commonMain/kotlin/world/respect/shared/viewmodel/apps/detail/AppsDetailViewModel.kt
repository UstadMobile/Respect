package world.respect.shared.viewmodel.apps.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import io.ktor.http.Url
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.apps_detail
import world.respect.shared.navigation.AppsDetail
import world.respect.shared.navigation.LearningUnitDetail
import world.respect.shared.navigation.LearningUnitList
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.RespectAppDataSource
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.compatibleapps.model.RespectAppManifest
import world.respect.lib.opds.model.OpdsGroup
import world.respect.lib.opds.model.OpdsPublication
import world.respect.lib.opds.model.ReadiumLink
import world.respect.datalayer.ext.dataOrNull
import world.respect.datalayer.school.model.SchoolApp
import world.respect.libutil.ext.resolve
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.navigation.NavCommand
import world.respect.shared.util.ext.asUiText
import world.respect.shared.util.ext.isAdmin

data class AppsDetailUiState(
    val appDetail: DataLoadState<RespectAppManifest>? = null,
    val publications: List<OpdsPublication> = emptyList(),
    val navigation: List<ReadiumLink> = emptyList(),
    val group: List<OpdsGroup> = emptyList(),
    val appIcon: String? = null,
    val isAdded: Boolean = false,
    val showAddRemoveButton: Boolean = false,
)

class AppsDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val appDataSource: RespectAppDataSource,
    accountManager: RespectAccountManager,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireSelectedAccountScope()

    private val schoolDataSource: SchoolDataSource by inject()

    private val _uiState = MutableStateFlow(AppsDetailUiState())

    val uiState = _uiState.asStateFlow()

    private val route: AppsDetail = savedStateHandle.toRoute()

    init {
        _appUiState.update {
            it.copy(
                title = Res.string.apps_detail.asUiText()
            )
        }

        viewModelScope.launch {
            appDataSource.compatibleAppsDataSource.getAppAsFlow(
                manifestUrl = route.manifestUrl,
                loadParams = DataLoadParams()
            ).collectLatest { result ->
                if (result is DataReadyState) {
                    _uiState.update {
                        it.copy(
                            appDetail = result,
                            appIcon = route.manifestUrl.resolve(
                                result.data.icon.toString()
                            ).toString()
                        )

                    }
                }

                result.dataOrNull()?.learningUnits?.also { learningUnitsUri ->
                    appDataSource.opdsDataSource.loadOpdsFeed(
                        url = route.manifestUrl.resolve(
                            learningUnitsUri.toString()
                        ),
                        params = DataLoadParams()
                    ).collect { result ->
                        when (result) {
                            is DataReadyState -> {
                                _uiState.update {
                                    it.copy(
                                        publications =
                                            result.data.publications?.map { publication ->
                                                publication.copy(
                                                    images = publication.images?.map { image ->
                                                        image.copy(
                                                            href = route.manifestUrl.resolve(image.href)
                                                                .toString()
                                                        )
                                                    }
                                                )
                                            } ?: emptyList(),

                                        navigation =
                                            result.data.navigation?.map { nav ->
                                                nav.copy(
                                                    href = route.manifestUrl.resolve(nav.href).toString(),
                                                    alternate = nav.alternate?.map { alt ->
                                                        alt.copy(
                                                            href = route.manifestUrl.resolve(alt.href)
                                                                .toString()
                                                        )
                                                    }
                                                )
                                            } ?: emptyList(),

                                        group =
                                            result.data.groups?.map { group ->
                                                group.copy(
                                                    publications = group.publications?.map { pub ->
                                                        pub.copy(
                                                            images = pub.images?.map { img ->
                                                                img.copy(
                                                                    href = route.manifestUrl.resolve(
                                                                        img.href
                                                                    )
                                                                        .toString()
                                                                )
                                                            }
                                                        )
                                                    },
                                                    navigation = group.navigation?.map { nav ->
                                                        nav.copy(
                                                            href = route.manifestUrl.resolve(nav.href)
                                                                .toString(),
                                                            alternate = nav.alternate?.map { alt ->
                                                                alt.copy(
                                                                    href = route.manifestUrl.resolve(
                                                                        alt.href
                                                                    )
                                                                        .toString()
                                                                )
                                                            }
                                                        )
                                                    }
                                                )
                                            } ?: emptyList(),
                                    )
                                }
                            }

                            else -> {}
                        }
                    }
                }
            }
        }

        viewModelScope.launch {
            accountManager.selectedAccountAndPersonFlow.collect { selected ->
                _uiState.update {
                    it.copy(showAddRemoveButton = selected?.person?.isAdmin() == true)
                }
            }
        }

        viewModelScope.launch {
            schoolDataSource.schoolAppDataSource.listAsFlow().map { list ->
                list.dataOrNull()?.any { it.appManifestUrl == route.manifestUrl } == true
            }.collect { appIsAdded ->
                _uiState.update {
                    it.copy(isAdded = appIsAdded)
                }
            }
        }
    }

    fun onClickLessonList() {

        val appManifest = uiState.value.appDetail?.dataOrNull()

        appManifest?.learningUnits?.toString()?.also { uri ->
            _navCommandFlow.tryEmit(
                NavCommand.Navigate(
                    LearningUnitList.create(
                        opdsFeedUrl = route.manifestUrl.resolve(uri),
                        appManifestUrl = route.manifestUrl,
                        resultPopUpTo = route.resultPopUpTo,
                        resultKey = route.resultKey,
                    )
                )
            )
        }
    }

    fun onClickPublication(publication: OpdsPublication) {

        val publicationHref = publication.links.find {
            it.rel?.equals(SELF) == true
        }?.href.toString()

        val refererUrl = uiState.value.appDetail?.dataOrNull()?.learningUnits.toString()

        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                LearningUnitDetail.create(
                    learningUnitManifestUrl = route.manifestUrl.resolve(publicationHref),
                    appManifestUrl = route.manifestUrl,
                    refererUrl = Url(refererUrl),
                    expectedIdentifier = publication.metadata.identifier.toString()
                )
            )
        )
    }

    fun onClickNavigation(navigation: ReadiumLink) {

        val navigationHref = navigation.href

        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                LearningUnitList.create(
                    opdsFeedUrl = route.manifestUrl.resolve(navigationHref),
                    appManifestUrl = route.manifestUrl,
                )
            )
        )
    }

    fun onClickTry() {
        /*TRY Button Click*/
    }

    fun onClickAdd() {
        viewModelScope.launch {
            schoolDataSource.schoolAppDataSource.store(
                listOf(
                    SchoolApp(
                        uid = route.manifestUrl.toString(),
                        appManifestUrl = route.manifestUrl,
                    )
                )
            )
        }
    }

    companion object {
        const val BUTTONS_ROW = "buttons_row"
        const val LESSON_HEADER = "lesson_header"
        const val SCREENSHOT = "screenshot"
        const val LEARNING_UNIT_LIST = "learning_unit_list"
        const val SELF = "self"
        const val APP_DETAIL = "app_detail"
    }
}
