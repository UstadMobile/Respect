package world.respect.shared.viewmodel.assignment.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.datalayer.SchoolDataSource
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.xapi.model.VERB_SAVED
import world.respect.lib.xapi.model.XapiGroup
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.DataLoadingState
import world.respect.lib.dataloadstate.DataReadyState
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.lib.dataloadstate.ext.firstOrNotLoaded
import world.respect.lib.dataloadstate.ext.isReadyAndSettled
import world.respect.lib.dataloadstate.ext.map
import world.respect.lib.opds.model.OpdsPublication
import world.respect.lib.xapi.ext.addActivityToContextActivitiesGrouping
import world.respect.lib.xapi.ext.mostRecentByTimestampOrNull
import world.respect.lib.xapi.ext.objectActivityNameOrNull
import world.respect.lib.xapi.ext.objectActivityOrNull
import world.respect.lib.xapi.ext.removeActivityFromContextActivitiesGrouping
import world.respect.lib.xapi.model.XapiActivity
import world.respect.lib.xapi.model.XapiStatement
import world.respect.lib.xapi.model.XapiVerb
import world.respect.lib.xapi.resources.XapiStatementsResource.GetStatementParams
import world.respect.libutil.ext.appendEndpointSegments
import world.respect.libutil.ext.isNullOrAllBlank
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.opds.getxapiactivityid.GetXapiActivityForPublicationUseCase
import world.respect.shared.domain.xapi.createBlankAssignmentStatement
import world.respect.shared.generated.resources.Res
import kotlin.time.Instant
import world.respect.shared.generated.resources.add_assignment
import world.respect.shared.generated.resources.edit_assignment
import world.respect.shared.generated.resources.required_field
import world.respect.shared.generated.resources.save
import world.respect.shared.navigation.AssignmentDetail
import world.respect.shared.navigation.AssignmentEdit
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.NavResultReturner
import world.respect.shared.navigation.RespectAppLauncher
import world.respect.shared.navigation.RouteResultDest
import world.respect.shared.resources.UiText
import world.respect.shared.util.LaunchDebouncer
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.ActionBarButtonUiState
import world.respect.shared.viewmodel.app.appstate.Snack
import world.respect.shared.viewmodel.app.appstate.SnackBarDispatcher
import world.respect.shared.viewmodel.learningunit.LearningUnitSelection
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class AssignmentEditUiState(
    val statementData: DataLoadState<XapiStatement> = DataLoadingState(),
    val assignee: String = "",
    val nameError: UiText? = null,
    val groupOptions: List<XapiGroup> = emptyList(),
    val groupError: UiText? = null,
    val learningUnitInfoFlow: (Url) -> Flow<DataLoadState<OpdsPublication>> = { flowOf(DataLoadingState()) },
) {
    val fieldsEnabled: Boolean
        get() = statementData.isReadyAndSettled()

    val hasErrors: Boolean
        get() = nameError != null || groupError != null
}

@OptIn(ExperimentalUuidApi::class)
class AssignmentEditViewModel(
    savedStateHandle: SavedStateHandle,
    private val accountManager: RespectAccountManager,
    private val json: Json,
    private val resultReturner: NavResultReturner,
    private val snackBarDispatcher: SnackBarDispatcher,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()

    private val route: AssignmentEdit = savedStateHandle.toRoute()

    private val schoolDataSource: SchoolDataSource by inject()

    private val getXapiActivityForPublicationUseCase: GetXapiActivityForPublicationUseCase by inject()

    private val _uiState = MutableStateFlow(
        AssignmentEditUiState(
            learningUnitInfoFlow = ::learningUnitInfoFlowFor
        )
    )

    val uiState = _uiState.asStateFlow()

    private val debouncer = LaunchDebouncer(viewModelScope)

    private val schoolUrl = accountManager.requireActiveSchoolUrl()

    private val assignmentActivityId = route.assignmentActivityId ?: run {
        schoolUrl.appendEndpointSegments(ACTIVITY_ID_PATH, Uuid.random().toString()).toString()
    }

    init {
        _appUiState.update { prev ->
            prev.copy(
                title = if (route.assignmentActivityId == null) {
                    Res.string.add_assignment.asUiText()
                } else {
                    Res.string.edit_assignment.asUiText()
                },
                userAccountIconVisible = false,
                actionBarButtonState = ActionBarButtonUiState(
                    visible = true,
                    text = Res.string.save.asUiText(),
                    onClick = ::onClickSave,
                ),
                hideBottomNavigation = true,
            )
        }

        launchWithLoadingIndicator(
            onShowError = { snackBarDispatcher.showSnackBar(Snack(it)) }
        ) {
            val statementResult = schoolDataSource.xapiStatementsResource.get(
                listParams = GetStatementParams(
                    verb = VERB_SAVED,
                ),
                dataLoadParams = DataLoadParams()
            ).dataOrNull()

            val allGroups = statementResult?.statements
                ?.filter { it.verb.id == VERB_SAVED }
                ?.sortedByDescending { it.timestamp ?: it.stored ?: Instant.DISTANT_PAST }
                ?.mapNotNull { it.`object` as? XapiGroup }
                ?.filter { group ->
                    group.account?.name != null &&
                    group.account?.homePage == schoolUrl.toString()
                }
                ?.distinctBy { it.account?.name }
                ?.map { group ->
                    val filteredMembers = group.member?.filter { agent ->
                        agent.name != null
                    }
                    group.copy(member = filteredMembers)
                } ?: emptyList()

            _uiState.update {
                it.copy(
                    groupOptions = allGroups,
                )
            }

            if (route.assignmentActivityId != null) {
                loadEntity(
                    json = json,
                    serializer = XapiStatement.serializer(),
                    loadFn = { params ->
                        schoolDataSource.xapiStatementsResource.get(
                            listParams = GetStatementParams(
                                activity = assignmentActivityId,
                                verb = XapiVerb.ID_ASSIGN,
                            ),
                            dataLoadParams = params
                        ).map { result ->
                            result.statements.mostRecentByTimestampOrNull()?.let {
                                listOf(it)
                            } ?: emptyList()
                        }.firstOrNotLoaded()
                    },
                    uiUpdateFn = { entity ->
                        _uiState.update { prev ->
                            prev.copy(
                                statementData = entity,
                                assignee = entity.dataOrNull()?.actor?.name.orEmpty()
                            )
                        }
                    }
                )
            } else {
                val instructor = accountManager.selectedAccountAndPersonFlow.first()?.xapiAgent
                    ?: return@launchWithLoadingIndicator
                val baseStmt = createBlankAssignmentStatement(
                    assignmentActivityId = assignmentActivityId,
                    instructor = instructor
                )
                val initialStmt = route.learningUnitSelected?.let {
                    val activity = getXapiActivityForPublicationUseCase(it.selectedPublication)
                    baseStmt.addActivityToContextActivitiesGrouping(activity)
                } ?: baseStmt

                _uiState.update { prev ->
                    prev.copy(
                        statementData = DataReadyState(initialStmt)
                    )
                }
            }

            viewModelScope.launch {
                resultReturner.filteredResultFlowForKey(KEY_LEARNING_UNIT).collect { result ->
                    val learningUnit = result.result as? LearningUnitSelection ?: return@collect
                    val activity = getXapiActivityForPublicationUseCase(learningUnit.selectedPublication)

                    _uiState.update { prev ->
                        val preStatementData = prev.statementData.dataOrNull() ?: return@update prev

                        prev.copy(
                            statementData = DataReadyState(
                                data = preStatementData.addActivityToContextActivitiesGrouping(
                                    activity
                                )
                            )
                        )
                    }
                }
            }
        }
    }

    fun learningUnitInfoFlowFor(url: Url): Flow<DataLoadState<OpdsPublication>> {
        return schoolDataSource.opdsPublicationDataSource.getByUrlAsFlow(
            url = url, params = DataLoadParams(), null, null
        )
    }

    fun onAssigneeGroupSelected(group: XapiGroup) {
        val statement = _uiState.value.statementData.dataOrNull() ?: return
        _uiState.update {
            it.copy(
                statementData = DataReadyState(
                    statement.copy(
                        actor = XapiGroup(
                            name = group.name,
                            account = group.account,
                        )
                    )
                ),
                assignee = group.name ?: "",
                groupError = null,
            )
        }
    }

    fun onEntityChanged(statement: XapiStatement) {
        _uiState.update { prev ->
            prev.copy(
                statementData = DataReadyState(statement),
                nameError = prev.nameError?.takeIf {
                    prev.statementData.dataOrNull()?.objectActivityNameOrNull() == statement.objectActivityNameOrNull()
                },
            )
        }

        debouncer.launch(DEFAULT_SAVED_STATE_KEY) {
            savedStateHandle[DEFAULT_SAVED_STATE_KEY] =
                json.encodeToString(XapiStatement.serializer(), statement)
        }
    }

    fun onAssigneeTextChanged(text: String) {
        _uiState.update {
            it.copy(assignee = text, groupError = null)
        }
    }


    fun onClickAddLearningUnit() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                RespectAppLauncher.create(
                    resultDest = RouteResultDest(
                        resultPopUpTo = route,
                        resultKey = KEY_LEARNING_UNIT,
                    )
                )
            )
        )
    }

    fun onClickRemoveLearningUnit(
        activity: XapiActivity
    ) {
        val assignment = uiState.value.statementData.dataOrNull() ?: return

        _uiState.update { prev ->
            prev.copy(
                statementData = DataReadyState(
                    data = assignment.removeActivityFromContextActivitiesGrouping(
                        idToRemove = activity.id
                    )
                )
            )
        }
    }

    fun onClickSave() {
        val stateToSave = _uiState.updateAndGet { prev ->
            val statement = prev.statementData.dataOrNull()

            prev.copy(
                nameError = Res.string.required_field.asUiText().takeIf {
                    statement?.objectActivityOrNull()?.definition?.name.isNullOrAllBlank()
                },
                groupError = Res.string.required_field.asUiText().takeIf {
                    statement?.actor?.name.isNullOrEmpty()
                }
            )
        }

        if (stateToSave.hasErrors)
            return

        val assignment = uiState.value.statementData.dataOrNull() ?: return

        launchWithLoadingIndicator {
            schoolDataSource.xapiStatementsResource.post(
                listOf(
                    assignment.copy(
                        id = Uuid.random(),
                        timestamp = Clock.System.now(),
                    )
                )
            )

            if (route.assignmentActivityId == null) {
                _navCommandFlow.tryEmit(
                    NavCommand.Navigate(
                        destination = AssignmentDetail(
                            assignmentActivityId = assignmentActivityId
                        ),
                        popUpTo = route,
                        popUpToInclusive = true,
                    )
                )
            } else {
                _navCommandFlow.tryEmit(NavCommand.PopUp())
            }
        }
    }

    companion object {

        const val KEY_LEARNING_UNIT = "result_learning_unit"

        const val ACTIVITY_ID_PATH = "xapi/activities/assignment"

    }
}
