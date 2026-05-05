package world.respect.shared.viewmodel.studentgrouping.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.ext.dataOrNull
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.xapi.model.VERB_SAVED
import world.respect.lib.xapi.model.VERB_VOIDED
import world.respect.lib.xapi.model.XapiAccount
import world.respect.lib.xapi.model.XapiAgent
import world.respect.lib.xapi.model.XapiGroup
import world.respect.lib.xapi.model.XapiObjectType
import world.respect.lib.xapi.model.XapiStatement
import world.respect.lib.xapi.model.XapiStatementRef
import world.respect.lib.xapi.model.XapiVerb
import world.respect.lib.xapi.resources.XapiStatementsResource
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.edit
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.NavResultReturner
import world.respect.shared.navigation.StudentGroupingDetail
import world.respect.shared.navigation.StudentGroupingEdit
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.lib.xapi.model.XapiGroup.Companion.RESULT_KEY_GROUP_UPDATED
import world.respect.lib.xapi.model.XapiGroup.Companion.CLASS
import world.respect.shared.viewmodel.app.appstate.FabUiState
import kotlin.getValue
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi


data class StudentGroupingDetailUiState(
    val groupName: String = "",
    val groupMembers: List<String> = emptyList(),
    val showDeleteGroupDialog: Boolean = false,
    val statementId: String? = null,
)

class StudentGroupingDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val respectAccountManager: RespectAccountManager,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = respectAccountManager.requireActiveAccountScope()

    private val _uiState = MutableStateFlow(StudentGroupingDetailUiState())

    val uiState = _uiState.asStateFlow()

    private val schoolDataSource: SchoolDataSource by inject()

    private val route: StudentGroupingDetail = savedStateHandle.toRoute()

    val schoolSelfUrl = respectAccountManager.activeAccount?.school?.self?.toString()
    val classActivityId = "${schoolSelfUrl}${CLASS}${route.classId}"

    init {
        _appUiState.update {
            it.copy(
                fabState = FabUiState(
                    visible = true,
                    icon = FabUiState.FabIcon.EDIT,
                    text = Res.string.edit.asUiText(),
                    onClick = ::onClickEdit
                )
            )
        }
        loadGroupDetail()

        val navResultReturner: NavResultReturner = getKoin().get()
        viewModelScope.launch {
            navResultReturner.filteredResultFlowForKey(
                RESULT_KEY_GROUP_UPDATED
            ).collect {
                loadGroupDetail()
            }
        }
    }

    fun onClickEdit() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                StudentGroupingEdit(
                    classUid = route.classId,
                    groupId = route.groupId
                )
            )
        )
    }

    fun onClickDeleteGroup() {
        _uiState.update { it.copy(showDeleteGroupDialog = true) }
    }

    fun onDismissDeleteGroupDialog() {
        _uiState.update { it.copy(showDeleteGroupDialog = false) }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun onConfirmDeleteGroup() {
        _uiState.update { it.copy(showDeleteGroupDialog = false) }

        viewModelScope.launch {
            try {
                // Fetch statements to find the one matching route.groupId
                val statementResult = schoolDataSource.xapiStatementsResource.get(
                    listParams = XapiStatementsResource.GetStatementParams(
                        verb = VERB_SAVED,
                        activity = classActivityId,
                        relatedActivities = true,
                    ),
                    dataLoadParams = DataLoadParams()
                ).dataOrNull()

                if (statementResult == null) {
                    Napier.w("onConfirmDeleteGroup: No statements found")
                    return@launch
                }

                // Get all voiding statements to filter out already voided statements
                val voidingStatements = schoolDataSource.xapiStatementsResource.get(
                    listParams = XapiStatementsResource.GetStatementParams(
                        verb = VERB_VOIDED,
                        activity = classActivityId,
                        relatedActivities = true,
                    ),
                    dataLoadParams = DataLoadParams()
                ).dataOrNull()?.statements ?: emptyList()

                // Extract IDs of voided statements
                val voidedStatementIds = voidingStatements
                    .mapNotNull { it.`object` as? XapiStatementRef }
                    .map { it.id }
                    .toSet()

                // Find the statement where group.account.name matches route.groupId
                val statementToVoid = statementResult.statements
                    .filter { statement ->
                        statement.id?.toString() !in voidedStatementIds
                    }
                    .firstOrNull { statement ->
                        val group = statement.`object` as? XapiGroup
                        group?.account?.name == route.groupId
                    }

                if (statementToVoid == null) {
                    Napier.w("onConfirmDeleteGroup: Statement with groupId ${route.groupId} not found")
                    return@launch
                }

                val statementId = statementToVoid.id?.toString()
                if (statementId == null) {
                    Napier.w("onConfirmDeleteGroup: Statement has no ID")
                    return@launch
                }

                val schoolSelfUrl = respectAccountManager.activeAccount?.school?.self

                val actor = XapiAgent(
                    name = respectAccountManager.activeAccount?.userGuid ?: "",
                    objectType = XapiObjectType.Agent,
                    account = XapiAccount(
                        name = respectAccountManager.activeAccount?.userGuid ?: "",
                        homePage = schoolSelfUrl.toString()
                    )
                )

                val verb = XapiVerb(
                    id = VERB_VOIDED,
                    display = mapOf("en" to "voided")
                )

                // Use StatementRef to reference the statement being voided
                val statementRef = XapiStatementRef(
                    objectType = XapiObjectType.StatementRef,
                    id = statementId
                )

                val voidingStatement = XapiStatement(
                    actor = actor,
                    verb = verb,
                    `object` = statementRef,
                    timestamp = Clock.System.now()
                )

                schoolDataSource.xapiStatementsResource.post(listOf(voidingStatement))

                _navCommandFlow.tryEmit(NavCommand.PopUp())

            } catch (e: Throwable) {
                Napier.e("onConfirmDeleteGroup ERROR", throwable = e)
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun loadGroupDetail() {
        viewModelScope.launch {
            try {
                schoolDataSource.xapiStatementsResource.getAsFlow(
                    listParams = XapiStatementsResource.GetStatementParams(
                        verb = VERB_SAVED,
                        activity = classActivityId,
                        relatedActivities = true,
                    ),
                    dataLoadParams = DataLoadParams()
                ).collect { dataLoadState ->
                    val statementResult = dataLoadState.dataOrNull() ?: return@collect

                    val groupStatement = statementResult.statements
                        .firstOrNull { statement ->
                            val group = statement.`object` as? XapiGroup
                            group?.account?.name == route.groupId
                        }

                    if (groupStatement != null) {
                        val group = groupStatement.`object` as XapiGroup
                        val memberNames = group.member?.mapNotNull { it.name } ?: emptyList()

                        _uiState.update { prev ->
                            prev.copy(
                                groupName = group.name ?: "",
                                groupMembers = memberNames,
                                statementId = groupStatement.id?.toString()
                            )
                        }

                        _appUiState.update {
                            it.copy(
                                title = group.name?.asUiText() ?: "".asUiText(),
                                userAccountIconVisible = false,
                                hideBottomNavigation = true,
                            )
                        }
                    }
                }
            } catch (e: Throwable) {
                Napier.e("loadGroupDetail ERROR", throwable = e)
            }
        }
    }
}
