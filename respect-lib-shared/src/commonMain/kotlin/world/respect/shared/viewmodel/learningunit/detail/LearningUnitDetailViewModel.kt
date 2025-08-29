package world.respect.shared.viewmodel.learningunit.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import world.respect.shared.navigation.LearningUnitDetail
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.DataLoadingState
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.RespectAppDataSource
import world.respect.datalayer.compatibleapps.model.RespectAppManifest
import world.respect.datalayer.ext.dataOrNull
import world.respect.datalayer.opds.model.OpdsPublication
import world.respect.datalayer.respect.model.LEARNING_UNIT_MIME_TYPES
import world.respect.libutil.ext.resolve
import world.respect.shared.domain.launchapp.LaunchAppUseCase
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.app.appstate.getTitle

data class LearningUnitDetailUiState(
    val lessonDetail: OpdsPublication? = null,
    val app: DataLoadState<RespectAppManifest> = DataLoadingState(),
)

class LearningUnitDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val appDataSource: RespectAppDataSource,
    private val launchAppUseCase: LaunchAppUseCase,
) : RespectViewModel(savedStateHandle) {

    private val _uiState = MutableStateFlow(LearningUnitDetailUiState())

    val uiState = _uiState.asStateFlow()

    private val route: LearningUnitDetail = savedStateHandle.toRoute()

    init {
        viewModelScope.launch {
            appDataSource.opdsDataSource.loadOpdsPublication(
                url = route.learningUnitManifestUrl,
                params = DataLoadParams(),
                referrerUrl = route.learningUnitManifestUrl,
                expectedPublicationId = route.expectedIdentifier
            ).collect { result ->
                when (result) {
                    is DataReadyState -> {
                        _uiState.update {
                            it.copy(
                                lessonDetail = result.data,
                            )
                        }
                        _appUiState.update {
                            it.copy(
                                title = result.data.metadata.title.getTitle().asUiText()
                            )
                        }
                    }
                    else -> {
                    }
                }
            }
        }

        viewModelScope.launch {
            appDataSource.compatibleAppsDataSource.getAppAsFlow(
                manifestUrl = route.appManifestUrl,
                loadParams = DataLoadParams()
            ).collect { app ->
                _uiState.update { it.copy(app = app) }
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
            learningUnitId = launchUrl,
            navigateFn = {
                _navCommandFlow.tryEmit(it)
            }
        )
    }

    companion object{
        const val IMAGE="image/png"
    }
}
