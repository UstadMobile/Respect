package world.respect.shared.viewmodel.learningunit.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.ustadmobile.libcache.PublicationPinState
import com.ustadmobile.libcache.UstadCache
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
import world.respect.datalayer.db.school.ext.isAdminOrTeacher
import world.respect.datalayer.school.opds.ext.hasRel
import world.respect.lib.dataloadstate.DataErrorResult
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.DataLoadingState
import world.respect.lib.dataloadstate.DataReadyState
import world.respect.lib.dataloadstate.NoDataLoadedState
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.lib.dataloadstate.ext.map
import world.respect.lib.opds.model.OpdsPublication
import world.respect.lib.opds.model.findSelfLinks
import world.respect.libutil.ext.resolve
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.launchapp.LaunchAppUseCase
import world.respect.shared.domain.licenses.GetLicenseLabelUseCase
import world.respect.shared.domain.school.LaunchCustomTabUseCase
import world.respect.shared.ext.tryOrShowSnackbarOnError
import world.respect.shared.navigation.AppsDetail
import world.respect.shared.navigation.AssignmentEdit
import world.respect.shared.navigation.LearningUnitDetail
import world.respect.shared.navigation.NavCommand
import world.respect.shared.resources.UiText
import world.respect.shared.util.exception.getUiTextOrGeneric
import world.respect.shared.util.ext.asUiText
import world.respect.shared.util.ext.resolve
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.Snack
import world.respect.shared.viewmodel.app.appstate.SnackBarDispatcher
import world.respect.shared.viewmodel.learningunit.LearningUnitSelection

data class LearningUnitDetailUiState(
    val lessonDetail: DataLoadState<OpdsPublication> = DataLoadingState(),
    val appDetail: OpdsPublication? = null,
    val pinState: PublicationPinState = PublicationPinState(
        PublicationPinState.Status.NOT_PINNED, 0, 0
    ),
    val showAssignButton: Boolean = false,
    val licenseLabel: UiText? = null,
) {
    val buttonsEnabled: Boolean
        get() = lessonDetail.dataOrNull() != null
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

    private val getLicenseLabelUseCase: GetLicenseLabelUseCase by inject()

    private val launchCustomTabUseCase: LaunchCustomTabUseCase by inject()


    init {
        _appUiState.update {
            it.copy(
                title = route.title?.asUiText() ?: it.title
            )
        }

        viewModelScope.launch {
            schoolDataSource.opdsPublicationDataSource.getByUrlAsFlow(
                url = route.learningUnitManifestUrl,
                params = DataLoadParams(),
                referrerUrl = route.learningUnitManifestUrl,
                expectedPublicationId = route.expectedIdentifier
            ).collect { result ->

                val lessonDetailMapped =
                    result.map { pub -> pub.resolve(route.learningUnitManifestUrl) }
                val finalLessonDetail = when (lessonDetailMapped) {
                    is NoDataLoadedState -> DataErrorResult(
                        error = IllegalStateException(),
                        metaInfo = lessonDetailMapped.metaInfo
                    )

                    else -> lessonDetailMapped
                }

                _uiState.update {
                    it.copy(
                        lessonDetail = finalLessonDetail,
                    )
                }

                if (result is DataReadyState) {
                    val lessonPublication = result.data.resolve(route.learningUnitManifestUrl)

                    // Load associated app
                    val appManifestHref = lessonPublication.links.firstOrNull {
                        it.hasRel(REL_LAUNCHABLE_APP)
                    }?.href

                    if (appManifestHref != null) {
                        schoolDataSource.opdsPublicationDataSource.getByUrlAsFlow(
                            url = route.learningUnitManifestUrl.resolve(appManifestHref),
                            params = DataLoadParams(),
                            referrerUrl = null,
                            expectedPublicationId = null,
                        ).collect { appResult ->

                            if (appResult is DataReadyState) {
                                val appPublication = appResult.data.resolve(
                                    route.learningUnitManifestUrl.resolve(appManifestHref)
                                )
                                val licenseLink =
                                    appPublication.links.firstOrNull { it.hasRel(LICENSE) }

                                _uiState.update {
                                    it.copy(
                                        appDetail = appPublication,
                                        licenseLabel = licenseLink?.let {
                                            getLicenseLabelUseCase(
                                                it
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        viewModelScope.launch {
            ustadCache.publicationPinState(route.learningUnitManifestUrl).collect { pinState ->
                _uiState.update { it.copy(pinState = pinState) }
            }
        }

        viewModelScope.launch {
            accountMananger.selectedAccountAndPersonFlow.collect { selectedAccount ->
                _uiState.update {
                    it.copy(showAssignButton = selectedAccount?.person?.isAdminOrTeacher() == true)
                }
            }
        }

    }


    fun onClickOpen() {
        Napier.d("LauncherAppViewModel: onClickOpen")
        //If app is null, then UiState.buttonsEnabled is false, so fallback return should never happen
        viewModelScope.launch {
            try {
                val lessonPublication =
                    _uiState.value.lessonDetail.dataOrNull()
                        ?: throw IllegalStateException("Not ready")

                launchAppUseCase(
                    LaunchAppUseCase.LaunchRequest(
                        publicationUrl = route.learningUnitManifestUrl,
                        publication = lessonPublication,
                        assignmentActivityId = route.assignmentActivityId,
                    )
                )
            } catch (e: Throwable) {
                Napier.w("Something wrong opening learning unit", e)
                snackBarDispatcher.showSnackBar(Snack(e.getUiTextOrGeneric()))
            }
        }
    }

    fun onClickDownload() {
        viewModelScope.launch {
            snackBarDispatcher.tryOrShowSnackbarOnError {
                when (uiState.value.pinState.status) {
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
        val publicationVal = uiState.value.lessonDetail.dataOrNull() ?: return

        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                destination = AssignmentEdit.create(
                    assignmentActivityId = null,
                    learningUnitSelected = LearningUnitSelection(
                        learningUnitManifestUrl = route.learningUnitManifestUrl,
                        selectedPublication = publicationVal,
                    )
                )
            )
        )
    }

    fun onClickApp(app: OpdsPublication) {
        val url = app.findSelfLinks().firstOrNull()?.href ?: return
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                destination = AppsDetail.create(manifestUrl = Url(url))
            )
        )
    }

    fun onClickLicense(app: OpdsPublication) {
        val licenseHref = app.links.firstOrNull { it.hasRel(LICENSE) }?.href
        if (licenseHref.isNullOrBlank()) {
            return
        }

        val appSelfLink = app.findSelfLinks().firstOrNull()?.href
        val licenseUrl = if (appSelfLink != null) {
            Url(appSelfLink).resolve(licenseHref)
        } else {
            Url(licenseHref)
        }

        try {
            launchCustomTabUseCase(licenseUrl)
        } catch (e: Throwable) {
            Napier.w("Something wrong opening license", e)
            snackBarDispatcher.showSnackBar(Snack(e.getUiTextOrGeneric()))
        }
    }

    private companion object {
        const val REL_LAUNCHABLE_APP =
            "https://id.openeel.org/rel/launchable-app"
        const val LICENSE = "license"
    }
}
