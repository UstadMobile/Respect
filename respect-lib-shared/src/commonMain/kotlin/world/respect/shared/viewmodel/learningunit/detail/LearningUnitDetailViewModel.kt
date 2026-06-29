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
import world.respect.datalayer.db.school.ext.isAdminOrTeacher
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.DataLoadingState
import world.respect.lib.dataloadstate.DataReadyState
import world.respect.lib.dataloadstate.ext.map
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.lib.opds.model.OpdsPublication
import world.respect.lib.xapi.model.XapiAccount
import world.respect.lib.xapi.model.XapiActivity
import world.respect.lib.xapi.model.XapiAgent
import world.respect.lib.xapi.model.XapiStatement
import world.respect.lib.xapi.model.XapiStatementRef
import world.respect.lib.xapi.model.XapiVerb
import world.respect.lib.xapi.resources.XapiStatementsResource
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
import world.respect.shared.viewmodel.learningunit.LearningUnitSelection

data class LearningUnitDetailUiState(
    val lessonDetail: OpdsPublication? = null,
    val app: DataLoadState<OpdsPublication> = DataLoadingState(),
    val pinState: PublicationPinState = PublicationPinState(
        PublicationPinState.Status.NOT_PINNED, 0, 0
    ),
    val showAssignButton: Boolean = false,
    val isBookmarked: Boolean = false,

    ) {
    val buttonsEnabled: Boolean
        get() = lessonDetail != null
}

class LearningUnitDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val ustadCache: UstadCache,
    val accountMananger: RespectAccountManager,
    private val snackBarDispatcher: SnackBarDispatcher,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {


    override val scope: Scope = accountMananger.requireActiveAccountScope()

    private val _uiState = MutableStateFlow(LearningUnitDetailUiState())

    val uiState = _uiState.asStateFlow()

    private val route: LearningUnitDetail = savedStateHandle.toRoute()

    private val schoolDataSource: SchoolDataSource by inject()

    private val launchAppUseCase: LaunchAppUseCase by inject()

    private val schoolUrl = accountMananger.requireActiveSchoolUrl()

    private val agent = XapiAgent(
        account = XapiAccount(
            homePage = schoolUrl.toString(),
            name = requireNotNull(accountMananger.activeAccount?.userGuid) {
                "LearningUnitDetailViewModel: active account userGuid must not be null"
            },
        )
    )

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
                                title = result.data.metadata.title.asUiText()
                            )
                        }
                    }
                    else -> {
                    }
                }
            }
        }

        viewModelScope.launch {
            schoolDataSource.xapiResource.statements.getAsFlow(
                listParams = XapiStatementsResource.GetStatementParams(
                    agent = agent,
                    verb = XapiVerb.ID_BOOKMARKED,
                    activity = route.learningUnitManifestUrl.toString(),
                ),
                dataLoadParams = DataLoadParams(),
            ).collect { result ->
                val statements = result.dataOrNull()?.statements ?: emptyList()
                _uiState.update { it.copy(isBookmarked = statements.isNotEmpty()) }
            }
        }

        viewModelScope.launch {
            schoolDataSource.opdsPublicationDataSource.getByUrlAsFlow(
                url = route.learningUnitManifestUrl,
                params = DataLoadParams(),
                referrerUrl = null,
                expectedPublicationId = null,
            ).collect { app ->
                    _uiState.update {
                        it.copy(
                            app = app.map { publication: OpdsPublication ->
                                publication.resolve(route.learningUnitManifestUrl)
                            }
                        )
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
                    assignmentActivityId = null,
                    learningUnitSelected = LearningUnitSelection(
                        learningUnitManifestUrl = route.learningUnitManifestUrl,
                        selectedPublication = publicationVal,
                    )
                )
            )
        )
    }

    fun onClickBookmark() {
        viewModelScope.launch {
            val learningUnitId = route.learningUnitManifestUrl.toString()

            if (uiState.value.isBookmarked) {
                // Void the existing bookmark statement(s)
                val existingStatements = schoolDataSource.xapiResource.statements.get(
                    listParams = XapiStatementsResource.GetStatementParams(
                        agent = agent,
                        verb = XapiVerb.ID_BOOKMARKED,
                        activity = learningUnitId,
                    )
                ).dataOrNull()?.statements ?: emptyList()

                existingStatements.forEach { stmt ->
                    val stmtId = stmt.id
                    if (stmtId == null) {
                        Napier.w("Cannot void bookmark: statement has no id")
                        return@forEach
                    }

                    schoolDataSource.xapiResource.statements.post(
                        listOf(
                            XapiStatement(
                                actor = agent,
                                verb = XapiVerb(id = XapiVerb.ID_VOIDED),
                                `object` = XapiStatementRef(id = stmtId.toString()),
                            )
                        )
                    )
                }
            } else {
                // Post a new bookmark statement
                val bookmarkStatement = XapiStatement(
                    actor = agent,
                    verb = XapiVerb(id = XapiVerb.ID_BOOKMARKED),
                    `object` = XapiActivity(id = learningUnitId),
                )
                schoolDataSource.xapiResource.statements.post(listOf(bookmarkStatement))
            }
        }
    }
}
