package world.respect.shared.viewmodel.apps.launcher

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.RespectAppDataSource
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.compatibleapps.model.RespectAppManifest
import world.respect.datalayer.ext.dataOrNull
import world.respect.datalayer.school.SchoolAppDataSource
import world.respect.datalayer.school.model.SchoolApp
import world.respect.datalayer.school.model.StatusEnum
import world.respect.datalayer.shared.paging.EmptyPagingSourceFactory
import world.respect.datalayer.shared.paging.IPagingSourceFactory
import world.respect.datalayer.shared.paging.PagingSourceFactoryHolder
import world.respect.libutil.ext.resolve
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.devmode.GetDevModeEnabledUseCase
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.app
import world.respect.shared.generated.resources.empty_list_description_admin
import world.respect.shared.generated.resources.empty_list_description_non_admin
import world.respect.shared.generated.resources.home
import world.respect.shared.navigation.AppsDetail
import world.respect.shared.navigation.LearningUnitList
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.Settings
import world.respect.shared.navigation.RespectAppLauncher
import world.respect.shared.navigation.RespectAppList
import world.respect.shared.navigation.NavResultReturner
import world.respect.shared.resources.UiText
import world.respect.shared.util.ext.asUiText
import world.respect.shared.util.ext.isAdmin
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.FabUiState
import world.respect.shared.viewmodel.curriculum.mapping.edit.CurriculumMappingEditViewModel
import world.respect.shared.viewmodel.curriculum.mapping.list.PlaylistListViewModel
import world.respect.shared.viewmodel.curriculum.mapping.model.CurriculumMapping

data class AppLauncherUiState(
    val apps: IPagingSourceFactory<Int, SchoolApp> = EmptyPagingSourceFactory(),
    val respectAppForSchoolApp: (SchoolApp) -> Flow<DataLoadState<RespectAppManifest>> = { emptyFlow() },
    val canRemove: Boolean = false,
    val emptyListDescription: UiText? = null,
    val selectedTabIndex: Int = 0,
)

class AppLauncherViewModel(
    savedStateHandle: SavedStateHandle,
    private val appDataSource: RespectAppDataSource,
    private val accountManager: RespectAccountManager,
    private val getDevModeEnabledUseCase: GetDevModeEnabledUseCase,
    private val json: Json,
    resultReturner: NavResultReturner,
    val playlistListViewModel: PlaylistListViewModel,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()

    private val _uiState = MutableStateFlow(AppLauncherUiState())

    val uiState = _uiState.asStateFlow()

    private var isAdmin: Boolean = false

    private val route: RespectAppLauncher = savedStateHandle.toRoute()

    private val schoolDataSource: SchoolDataSource by inject()

    private val pagingSourceHolder = PagingSourceFactoryHolder {
        schoolDataSource.schoolAppDataSource.listAsPagingSource(
            loadParams = DataLoadParams(),
            params = SchoolAppDataSource.GetListParams()
        )
    }
    init {
        _appUiState.update {
            it.copy(
                title = Res.string.home.asUiText(),
                onClickSettings = ::onClickSettings,
                hideBottomNavigation = route.resultDest != null,
                showBackButton = route.resultDest != null,
            )
        }

        _uiState.update { prev ->
            prev.copy(
                respectAppForSchoolApp = this@AppLauncherViewModel::respectAppForSchoolApp,
                apps = pagingSourceHolder,
            )
        }
        viewModelScope.launch {
            playlistListViewModel.navCommandFlow.collect { navCommand ->
                _navCommandFlow.tryEmit(navCommand)
            }
        }

        val savedMappings = loadMappingsFromSavedState(savedStateHandle)
        playlistListViewModel.setMappings(savedMappings)
        viewModelScope.launch {
            playlistListViewModel.uiState.collect { state ->
                cachedPlaylists = state.mappings
            }
        }

        viewModelScope.launch {
            accountManager.selectedAccountAndPersonFlow.collect { selected ->
                val isAdmin = selected?.person?.isAdmin() == true
                val devModeEnabled = getDevModeEnabledUseCase()

                this@AppLauncherViewModel.isAdmin = isAdmin
                updateFabState(isAdmin, _uiState.value.selectedTabIndex)

                _appUiState.update {
                    it.copy(
                        settingsIconVisible = isAdmin && devModeEnabled,
                    )
                }
                _uiState.update {
                    it.copy(
                        canRemove = isAdmin,
                        emptyListDescription = if(isAdmin)
                            Res.string.empty_list_description_admin.asUiText()
                        else
                            Res.string.empty_list_description_non_admin.asUiText()
                    )
                }
            }
        }
    }
    fun onTabSelected(index: Int) {
        _uiState.update { it.copy(selectedTabIndex = index) }
        updateFabState(isAdmin, index)
    }

    private fun updateFabState(isAdmin: Boolean, tabIndex: Int) {
        _appUiState.update {
            it.copy(
                fabState = when (tabIndex) {
                    0 -> FabUiState(
                        visible = isAdmin,
                        icon = FabUiState.FabIcon.ADD,
                        text = Res.string.app.asUiText(),
                        onClick = {
                            _navCommandFlow.tryEmit(
                                NavCommand.Navigate(RespectAppList)
                            )
                        }
                    )
                    1 -> FabUiState(visible = false)
                    else -> FabUiState(visible = false)
                }
            )
        }
    }

    private fun loadMappingsFromSavedState(savedStateHandle: SavedStateHandle): List<CurriculumMapping> {
        val mappingsJson = savedStateHandle.get<String>(KEY_MAPPINGS_LIST) ?: return emptyList()
        return try {
            json.decodeFromString<List<CurriculumMapping>>(mappingsJson)
        } catch (e: Exception) {
            emptyList()
        }
    }
    fun onClickApp(app: DataLoadState<RespectAppManifest>) {
        val url = app.metaInfo.url ?: return
        val appData = app.dataOrNull() ?: return

        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                if(route.resultDest != null) {
                    LearningUnitList.create(
                        opdsFeedUrl = url.resolve(appData.learningUnits.toString()),
                        appManifestUrl = url,
                        resultDest = route.resultDest,
                    )
                }else {
                    AppsDetail.create(
                        manifestUrl = url,
                        resultDest = route.resultDest,
                    )
                }
            )
        )
    }

    fun onClickSettings() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(Settings)
        )
    }

    fun onClickRemove(app: DataLoadState<RespectAppManifest>) {
        val manifestUrl = app.metaInfo.url ?: return
        viewModelScope.launch {
            schoolDataSource.schoolAppDataSource.store(
                listOf(
                    SchoolApp(
                        uid = manifestUrl.toString(),
                        appManifestUrl = manifestUrl,
                        status = StatusEnum.TO_BE_DELETED
                    )
                )
            )
        }
    }

    fun respectAppForSchoolApp(schoolApp: SchoolApp): Flow<DataLoadState<RespectAppManifest>> {
        return appDataSource.compatibleAppsDataSource.getAppAsFlow(
            schoolApp.appManifestUrl,
            DataLoadParams()
        )
    }

    companion object {
        const val KEY_MAPPINGS_LIST = "mappings_list"
        var cachedPlaylists: List<CurriculumMapping> = emptyList()
    }
}