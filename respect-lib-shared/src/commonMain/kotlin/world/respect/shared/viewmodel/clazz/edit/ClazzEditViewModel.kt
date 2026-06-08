package world.respect.shared.viewmodel.clazz.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.DataLoadingState
import world.respect.lib.dataloadstate.DataReadyState
import world.respect.datalayer.SchoolDataSource
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.lib.dataloadstate.ext.isReadyAndSettled
import world.respect.lib.dataloadstate.ext.firstOrNotLoaded
import world.respect.lib.dataloadstate.ext.map
import world.respect.lib.xapi.ext.mostRecentByTimestampOrNull
import world.respect.lib.xapi.model.XapiStatement
import world.respect.lib.xapi.resources.XapiStatementsResource.GetStatementParams
import world.respect.libutil.ext.appendEndpointSegments
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.xapi.ACTIVITY_ID_PATH
import world.respect.shared.domain.xapi.classDefinitionTitle
import world.respect.shared.domain.xapi.createBlankClassStatement
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.add_clazz
import world.respect.shared.generated.resources.edit_clazz
import world.respect.shared.generated.resources.required_field
import world.respect.shared.generated.resources.save
import world.respect.shared.navigation.ClazzDetail
import world.respect.shared.navigation.ClazzEdit
import world.respect.shared.navigation.NavCommand
import world.respect.shared.resources.UiText
import world.respect.shared.util.LaunchDebouncer
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.ActionBarButtonUiState
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


data class ClazzEditUiState(
    val statementData: DataLoadState<XapiStatement> = DataLoadingState(),
    val clazzNameError: UiText? = null,
) {
    val fieldsEnabled: Boolean
        get() = statementData.isReadyAndSettled()

    val hasErrors: Boolean
        get() = clazzNameError != null
}

@OptIn(ExperimentalUuidApi::class)
class ClazzEditViewModel(
    savedStateHandle: SavedStateHandle,
    private val accountManager: RespectAccountManager,
    private val json: Json,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()

    private val schoolDataSource: SchoolDataSource by inject()
    private val route: ClazzEdit = savedStateHandle.toRoute()

    private val _uiState = MutableStateFlow(ClazzEditUiState())

    val uiState = _uiState.asStateFlow()

    private val schoolUrl = accountManager.requireActiveSchoolUrl()

    private val classActivityId = route.classActivityId ?: run {
        schoolUrl.appendEndpointSegments(
            ACTIVITY_ID_PATH,
            Uuid.random().toString()
        ).toString()
    }

    private val debouncer = LaunchDebouncer(viewModelScope)

    init {
        _appUiState.update { prev ->
            prev.copy(
                title = if (route.classActivityId == null) {
                    Res.string.add_clazz.asUiText()
                } else {
                    Res.string.edit_clazz.asUiText()
                },
                userAccountIconVisible = false,
                actionBarButtonState = ActionBarButtonUiState(
                    visible = true,
                    text = Res.string.save.asUiText(),
                    onClick = ::onClickSave
                ),
                hideBottomNavigation = true,
            )
        }

        launchWithLoadingIndicator {
            if (route.classActivityId != null) {
                loadEntity(
                    json = json,
                    serializer = XapiStatement.serializer(),
                    loadFn = { params ->
                        schoolDataSource.xapiStatementsResource.get(
                            listParams = GetStatementParams(
                                activity = classActivityId,
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
                                statementData = entity
                            )
                        }
                    }
                )
            } else {
                val actor = accountManager.selectedAccountAndPersonFlow.firstOrNull()?.xapiAgent
                    ?: return@launchWithLoadingIndicator
                val baseStmt = createBlankClassStatement(
                    classActivityId = classActivityId,
                    actor = actor
                )

                _uiState.update { prev ->
                    prev.copy(
                        statementData = DataReadyState(baseStmt)
                    )
                }
            }
        }
    }

    fun onEntityChanged(statement: XapiStatement) {
        _uiState.update { prev ->
            prev.copy(
                statementData = DataReadyState(statement),
                clazzNameError = prev.clazzNameError?.takeIf {
                    prev.statementData.dataOrNull()?.classDefinitionTitle == statement.classDefinitionTitle
                },
            )
        }

        debouncer.launch(DEFAULT_SAVED_STATE_KEY) {
            savedStateHandle[DEFAULT_SAVED_STATE_KEY] =
                json.encodeToString(XapiStatement.serializer(), statement)
        }
    }

    fun onClickSave() {
        val stateToSave = _uiState.updateAndGet { prev ->
            val statement = prev.statementData.dataOrNull()

            prev.copy(
                clazzNameError = Res.string.required_field.asUiText().takeIf {
                    statement?.classDefinitionTitle.isNullOrBlank()
                },
            )
        }

        if (stateToSave.hasErrors)
            return

        val classStatement = _uiState.value.statementData.dataOrNull() ?: return

        launchWithLoadingIndicator {
            schoolDataSource.xapiStatementsResource.post(
                listOf(
                    classStatement.copy(
                        id = Uuid.random(),
                        timestamp = Clock.System.now(),
                    )
                )
            )

            if (route.classActivityId == null) {
                _navCommandFlow.tryEmit(
                    NavCommand.Navigate(
                        destination = ClazzDetail(
                            classActivityId = classActivityId,
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

    fun onClearError() {
        _uiState.update { prev -> prev.copy(clazzNameError = null) }
    }
}