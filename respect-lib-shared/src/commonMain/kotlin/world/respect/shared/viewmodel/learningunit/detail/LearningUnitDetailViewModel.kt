package world.respect.shared.viewmodel.learningunit.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import world.respect.datalayer.DataErrorResult
import world.respect.shared.navigation.LearningUnitDetail
import world.respect.shared.datasource.RespectAppDataSourceProvider
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.DataLoadingState
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.compatibleapps.model.RespectAppManifest
import world.respect.datalayer.ext.dataOrNull
import world.respect.datalayer.opds.model.OpdsPublication
import world.respect.datalayer.respect.model.LEARNING_UNIT_MIME_TYPES
import world.respect.libutil.ext.resolve
import world.respect.shared.domain.launchapp.LaunchAppUseCase
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.something_went_wrong
import world.respect.shared.viewmodel.app.appstate.getTitle

data class LearningUnitDetailUiState(
    val lessonDetail: OpdsPublication? = null,
    val app: DataLoadState<RespectAppManifest> = DataLoadingState(),
    val isLoading: Boolean = true,
    val snackBarMessage: String? = null
)

class LearningUnitDetailViewModel(
    savedStateHandle: SavedStateHandle,
    dataSourceProvider: RespectAppDataSourceProvider,
    private val launchAppUseCase: LaunchAppUseCase,
) : RespectViewModel(savedStateHandle) {

    private val dataSource = dataSourceProvider.getDataSource(activeAccount)

    private val _uiState = MutableStateFlow(LearningUnitDetailUiState())

    val uiState = _uiState.asStateFlow()

    private val route: LearningUnitDetail = savedStateHandle.toRoute()

    init {
        viewModelScope.launch {
            dataSource.opdsDataSource.loadOpdsPublication(
                url = route.learningUnitManifestUrl,
                params = DataLoadParams(),
                referrerUrl = route.learningUnitManifestUrl,
                expectedPublicationId = route.expectedIdentifier
            ).collect { result ->
                when (result) {

                    is DataLoadingState -> {
                        _uiState.update {
                            it.copy(
                                isLoading = true
                            )
                        }
                    }

                    is DataReadyState -> {
                        _uiState.update {
                            it.copy(
                                lessonDetail = result.data,
                                isLoading = false
                            )
                        }
                        _appUiState.update {
                            it.copy(
                                title = result.data.metadata.title.getTitle()
                            )
                        }
                    }

                    is DataErrorResult -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                snackBarMessage = getString(resource = Res.string.something_went_wrong)
                            )
                        }
                    }

                    else -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false
                            )
                        }
                    }
                }
            }
        }
        viewModelScope.launch {
            dataSource.compatibleAppsDataSource.getAppAsFlow(
                manifestUrl = route.appManifestUrl,
                loadParams = DataLoadParams()
            ).collect { app ->
                _uiState.update {
                    it.copy(
                        app = app,
                        isLoading = false
                    )
                }
            }
        }
    }


    fun onClickOpen() {
        val respectApp = _uiState.value.app.dataOrNull() ?: return
        val launchLink = _uiState.value.lessonDetail?.links?.firstOrNull { link ->
            link.rel?.any { it.startsWith("http://opds-spec.org/acquisition") } == true &&
                    LEARNING_UNIT_MIME_TYPES.any { link.type?.startsWith(it) == true }
        } ?: return

        val launchUrl = route.learningUnitManifestUrl.resolve(launchLink.href)

        launchAppUseCase(
            app = respectApp,
            account = activeAccount,
            learningUnitId = launchUrl,
            navigateFn = {
                _navCommandFlow.tryEmit(it)
            }
        )
    }

    fun onClearSnackBar() {
        _uiState.update {
            it.copy(
                snackBarMessage = null
            )
        }
    }

    companion object {
        const val IMAGE = "image/png"
    }
}
