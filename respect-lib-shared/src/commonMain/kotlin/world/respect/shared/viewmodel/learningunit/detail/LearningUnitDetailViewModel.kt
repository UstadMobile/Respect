package world.respect.shared.viewmodel.learningunit.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.ustadmobile.libcache.PublicationPinState
import com.ustadmobile.libcache.UstadCache
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.shared.navigation.LearningUnitDetail
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.DataLoadingState
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.RespectAppDataSource
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.compatibleapps.model.RespectAppManifest
import world.respect.datalayer.ext.dataOrNull
import world.respect.lib.opds.model.OpdsPublication
import world.respect.datalayer.respect.model.LEARNING_UNIT_MIME_TYPES
import world.respect.libutil.ext.resolve
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.launchapp.LaunchAppUseCase
import world.respect.shared.navigation.AssignmentEdit
import world.respect.shared.navigation.NavCommand
import world.respect.shared.util.ext.asUiText
import world.respect.shared.util.ext.resolve
import world.respect.shared.viewmodel.app.appstate.getTitle
import world.respect.shared.viewmodel.learningunit.LearningUnitSelection

data class LearningUnitDetailUiState(
    val lessonDetail: OpdsPublication? = null,
    val app: DataLoadState<OpdsPublication> = DataLoadingState(),
    val pinState: PublicationPinState = PublicationPinState(
        PublicationPinState.Status.NOT_PINNED, 0, 0
    ),
) {
    val buttonsEnabled: Boolean
        get() = lessonDetail != null
}

class LearningUnitDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val appDataSource: RespectAppDataSource,
    private val launchAppUseCase: LaunchAppUseCase,
    private val ustadCache: UstadCache,
    accountMananger: RespectAccountManager,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {


    override val scope: Scope = accountMananger.requireActiveAccountScope()

    private val _uiState = MutableStateFlow(LearningUnitDetailUiState())

    val uiState = _uiState.asStateFlow()

    private val route: LearningUnitDetail = savedStateHandle.toRoute()

    private val schoolDataSource: SchoolDataSource by inject()

    init {
        viewModelScope.launch {
            schoolDataSource.opdsDataSource.loadOpdsPublication(
                url = route.learningUnitManifestUrl,
                params = DataLoadParams(),
                referrerUrl = route.learningUnitManifestUrl,
                expectedPublicationId = route.expectedIdentifier
            ).collect { result ->
                when (result) {
                    is DataReadyState -> {
                        _uiState.update {
                            it.copy(
                                lessonDetail = result.data.resolve(
                                    route.learningUnitManifestUrl
                                )
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
            schoolDataSource.opdsDataSource.loadOpdsPublication(
                url = route.appManifestUrl,
                params = DataLoadParams(),
                referrerUrl = null,
                expectedPublicationId = null,
            ).collect { app ->
                _uiState.update { it.copy(app = app) }
            }
        }

        viewModelScope.launch {
            ustadCache.publicationPinState(route.learningUnitManifestUrl).collect { pinState ->
                _uiState.update { it.copy(pinState = pinState) }
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

    fun onClickDownload() {
        viewModelScope.launch {
            try {
                when(uiState.value.pinState.status) {
                    PublicationPinState.Status.NOT_PINNED -> {
                        ustadCache.pinPublication(route.learningUnitManifestUrl)
                    }
                    PublicationPinState.Status.READY -> {
                        ustadCache.unpinPublication(route.learningUnitManifestUrl)
                    }
                    else -> {
                        //Do nothing
                    }
                }

            }catch(t: Throwable) {
                t.printStackTrace()
            }
        }
    }

    fun onClickAssign() {
        val publicationVal = uiState.value.lessonDetail ?: return

        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                destination = AssignmentEdit.create(
                    uid = null,
                    learningUnitSelected = LearningUnitSelection(
                        learningUnitManifestUrl = route.learningUnitManifestUrl,
                        selectedPublication = publicationVal,
                        appManifestUrl = route.appManifestUrl
                    )
                )
            )
        )
    }
}
