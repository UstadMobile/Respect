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
import world.respect.datalayer.school.ClassDataSource
import world.respect.datalayer.school.model.AssignmentLearningUnitRef
import world.respect.datalayer.school.model.Clazz
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.DataLoadingState
import world.respect.lib.dataloadstate.DataReadyState
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.lib.dataloadstate.ext.isReadyAndSettled
import world.respect.lib.dataloadstate.ext.map
import world.respect.lib.opds.model.OpdsPublication
import world.respect.lib.xapi.model.VERB_ASSIGN
import world.respect.lib.xapi.model.XapiStatement
import world.respect.lib.xapi.resources.XapiStatementsResource.GetStatementParams
import world.respect.libutil.ext.appendEndpointSegments
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.xapi.activityDefinitionTitle
import world.respect.shared.domain.xapi.actorName
import world.respect.shared.domain.xapi.assignmentLearningUnits
import world.respect.shared.domain.xapi.createBlankAssignmentStatement
import world.respect.shared.domain.xapi.isAssignmentStatement
import world.respect.shared.domain.xapi.withLearningUnits
import world.respect.shared.ext.studentsXapiGroup
import world.respect.shared.generated.resources.Res
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
import world.respect.shared.viewmodel.learningunit.LearningUnitSelection
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class AssignmentEditUiState(
    val statementData: DataLoadState<XapiStatement> = DataLoadingState(),
    val assignee: String = "",
    val nameError: UiText? = null,
    val classOptions: List<Clazz> = emptyList(),
    val classError: UiText? = null,
    val learningUnitInfoFlow: (Url) -> Flow<DataLoadState<OpdsPublication>> = { flowOf(DataLoadingState()) },
) {
    val fieldsEnabled: Boolean
        get() = statementData.isReadyAndSettled()

    val hasErrors: Boolean
        get() = nameError != null || classError != null
}

@OptIn(ExperimentalUuidApi::class)
class AssignmentEditViewModel(
    savedStateHandle: SavedStateHandle,
    private val accountManager: RespectAccountManager,
    private val json: Json,
    private val resultReturner: NavResultReturner,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()

    private val route: AssignmentEdit = savedStateHandle.toRoute()

    private val schoolDataSource: SchoolDataSource by inject()

    private val _uiState = MutableStateFlow(AssignmentEditUiState())

    val uiState = _uiState.asStateFlow()

    private val debouncer = LaunchDebouncer(viewModelScope)

    private val schoolUrl = accountManager.activeAccount?.school?.self
        ?: throw IllegalStateException("No active school account found")

    private val assignmentActivityId = route.assignmentActivityId ?: run {
        schoolUrl.appendEndpointSegments(ACTIVITY_ID_PATH, Uuid.random().toString()).toString()
    }
    private fun LearningUnitSelection.toRef(): AssignmentLearningUnitRef {
        return AssignmentLearningUnitRef(
            learningUnitManifestUrl = this.learningUnitManifestUrl,
            appManifestUrl = this.appManifestUrl,
        )
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
        launchWithLoadingIndicator {
            val classes = schoolDataSource.classDataSource.list(
                DataLoadParams(),
                ClassDataSource.GetListParams()
            ).dataOrNull() ?: emptyList()

            _uiState.update {
                it.copy(
                    classOptions = classes,
                    learningUnitInfoFlow = ::learningUnitInfoFlowFor
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
                                verb = VERB_ASSIGN
                            ),
                            dataLoadParams = params
                        ).map { result ->
                            result.statements
                                .filter { it.isAssignmentStatement }
                                .maxByOrNull {
                                    (it.timestamp ?: it.stored)?.toEpochMilliseconds()
                                        ?: Long.MIN_VALUE
                                }
                                ?: throw IllegalStateException()
                        }
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
                val instructor = accountManager.selectedAccountAndPersonFlow.first()?.xapiAgent ?: return@launchWithLoadingIndicator
                _uiState.update { prev ->
                    prev.copy(
                        statementData = DataReadyState(
                            createBlankAssignmentStatement(
                                assignmentActivityId = assignmentActivityId,
                                instructor = instructor
                            ).let { stmt ->
                                route.learningUnitSelected?.let {
                                    stmt.withLearningUnits(listOf(it.toRef()))
                                } ?: stmt
                            }
                        )
                    )
                }
            }

            viewModelScope.launch {
                resultReturner.filteredResultFlowForKey(KEY_LEARNING_UNIT).collect { result ->
                    val learningUnit = result.result as? LearningUnitSelection ?: return@collect
                    val assignmentResourceRef = learningUnit.toRef()

                    _uiState.update { prev ->
                        val preStatementData = prev.statementData.dataOrNull() ?: return@update prev

                        prev.copy(
                            statementData = DataReadyState(
                                data = preStatementData.withLearningUnits(
                                    preStatementData.assignmentLearningUnits + assignmentResourceRef
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

    fun onAssigneeClassSelected(clazz: Clazz) {
        val statement = _uiState.value.statementData.dataOrNull() ?: return
        _uiState.update {
            it.copy(
                statementData = DataReadyState(
                    statement.copy(actor = clazz.studentsXapiGroup(schoolUrl))
                ),
                assignee = clazz.title,
                classError = null,
            )
        }
    }

    fun onEntityChanged(statement: XapiStatement) {
        _uiState.update { prev ->
            prev.copy(
                statementData = DataReadyState(statement),
                nameError = prev.nameError?.takeIf {
                    prev.statementData.dataOrNull()?.activityDefinitionTitle == statement.activityDefinitionTitle
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
            it.copy(assignee = text, classError = null)
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
        ref: AssignmentLearningUnitRef
    ) {
        val assignment = uiState.value.statementData.dataOrNull() ?: return

        _uiState.update { prev ->
            prev.copy(
                statementData = DataReadyState(
                    data = assignment.withLearningUnits(
                        assignment.assignmentLearningUnits.filter {
                            it.learningUnitManifestUrl != ref.learningUnitManifestUrl
                        }
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
                    statement?.activityDefinitionTitle.isNullOrBlank()
                },
                classError = Res.string.required_field.asUiText().takeIf {
                    statement?.actorName.isNullOrEmpty()
                }
            )
        }

        if (stateToSave.hasErrors)
            return

        val assignment = uiState.value.statementData.dataOrNull() ?: return

        launchWithLoadingIndicator {
            val updatedStatement = assignment.copy(timestamp = Clock.System.now())

            schoolDataSource.xapiStatementsResource.post(listOf(updatedStatement))

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
