package world.respect.shared.viewmodel.learningunit.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.ustadmobile.libcache.PublicationPinState
import com.ustadmobile.libcache.UstadCache
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.datalayer.SchoolDataSource
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataReadyState
import world.respect.lib.opds.model.OpdsPublication
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.launchapp.LaunchAppUseCase
import world.respect.shared.ext.tryOrShowSnackbarOnError
import world.respect.shared.navigation.AssignmentEdit
import world.respect.shared.navigation.LearningUnitDetail
import world.respect.shared.navigation.NavCommand
import world.respect.shared.util.exception.getUiTextOrGeneric
import world.respect.shared.util.ext.asUiText
import world.respect.shared.util.ext.resolve
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.Snack
import world.respect.shared.viewmodel.app.appstate.SnackBarDispatcher
import world.respect.shared.viewmodel.app.appstate.getTitle
import world.respect.shared.viewmodel.learningunit.LearningUnitSelection

data class LearningUnitDetailUiState(
    val lessonDetail: OpdsPublication? = null,
    val pinState: PublicationPinState = PublicationPinState(
        PublicationPinState.Status.NOT_PINNED, 0, 0
    ),
) {
    val buttonsEnabled: Boolean
        get() = lessonDetail != null
}

class LearningUnitDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val ustadCache: UstadCache,
    accountMananger: RespectAccountManager,
    private val snackBarDispatcher: SnackBarDispatcher,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {


    override val scope: Scope = accountMananger.requireActiveAccountScope()

    private val _uiState = MutableStateFlow(LearningUnitDetailUiState())

    val uiState = _uiState.asStateFlow()

    private val route: LearningUnitDetail = savedStateHandle.toRoute()

    private val schoolDataSource: SchoolDataSource by inject()

    private val launchAppUseCase: LaunchAppUseCase by inject()

    init {
        viewModelScope.launch {
            schoolDataSource.opdsPublicationDataSource.getByUrlAsFlow(
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
            ustadCache.publicationPinState(route.learningUnitManifestUrl).collect { pinState ->
                _uiState.update { it.copy(pinState = pinState) }
            }
        }

    }


    fun onClickOpen() {
        //If app is null, then UiState.buttonsEnabled is false, so fallback return should never happen
        viewModelScope.launch {
            try {
                val lessonPublication = _uiState.value.lessonDetail ?: throw IllegalStateException("Not ready")

                launchAppUseCase(
                    LaunchAppUseCase.LaunchRequest(
                        publicationUrl = route.learningUnitManifestUrl,
                        publication = lessonPublication,
                        assignmentActivityId = route.assignmentActivityId,
                    )
                )
            }catch(e: Throwable) {
                Napier.w("Something wrong opening learning unit", e)
                snackBarDispatcher.showSnackBar(Snack(e.getUiTextOrGeneric()))
            }
        }
    }

    fun onClickDownload() {
        viewModelScope.launch {
            snackBarDispatcher.tryOrShowSnackbarOnError {
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
                    )
                )
            )
        )
    }
}
