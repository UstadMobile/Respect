package world.respect.shared.viewmodel.studentgrouping.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.datalayer.SchoolDataSource
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.xapi.model.VERB_SAVED
import world.respect.lib.xapi.model.VERB_VOIDED
import world.respect.lib.xapi.model.XapiAccount
import world.respect.lib.xapi.model.XapiAgent
import world.respect.lib.xapi.model.XapiGroup
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
import world.respect.lib.xapi.model.XapiObjectType
import world.respect.lib.xapi.model.XapiStatement
import world.respect.lib.xapi.model.XapiStatementRef
import world.respect.lib.xapi.model.XapiVerb
import world.respect.shared.viewmodel.app.appstate.FabUiState
import kotlin.getValue
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi

data class StudentGroupingDetailUiState(
    val groupName: String = "",
    val groupMembers: List<String> = emptyList(),
    val showDeleteGroupDialog: Boolean = false,
    val statementGroupId: String? = null,
    val classId: String? = null,
)

class StudentGroupingDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val respectAccountManager: RespectAccountManager,
    private val navResultReturner: NavResultReturner
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = respectAccountManager.requireActiveAccountScope()

    private val _uiState = MutableStateFlow(StudentGroupingDetailUiState())

    val uiState = _uiState.asStateFlow()

    private val schoolDataSource: SchoolDataSource by inject()

    private val route: StudentGroupingDetail = savedStateHandle.toRoute()

    private val schoolSelfUrl = respectAccountManager.activeAccount?.school?.self
        ?: throw IllegalStateException("schoolSelfUrl is required")


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

        viewModelScope.launch {
            navResultReturner.filteredResultFlowForKey(
                RESULT_KEY_GROUP_UPDATED
            ).collect {
                loadGroupDetail()
            }
        }
    }

    fun onClickEdit() {
        val classId = _uiState.value.classId
        if (classId == null) {
            Napier.e("onClickEdit: classId not loaded when trying to edit group")
            return
        }
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                StudentGroupingEdit(
                    classUid = classId,
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

        launchWithLoadingIndicator {

            val statementId = _uiState.value.statementGroupId
                ?: throw IllegalStateException("Statement ID not found when trying to delete group")

            val sessionAndPerson = respectAccountManager.selectedAccountAndPersonFlow.firstOrNull()
                ?: throw IllegalStateException("No person selected when trying to delete group")

            val actor = sessionAndPerson.xapiAgent

            val voidingStatement = XapiStatement(
                actor = actor,
                verb = XapiVerb(id = VERB_VOIDED),
                `object` = XapiStatementRef(
                    objectType = XapiObjectType.StatementRef,
                    id = statementId
                ),
                timestamp = Clock.System.now()
            )
            schoolDataSource.xapiStatementsResource.post(listOf(voidingStatement))
            _navCommandFlow.tryEmit(NavCommand.PopUp())

        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun loadGroupDetail() {
        viewModelScope.launch {
            // Query by agent (the group itself) using its account identifier
            val groupAgent = XapiAgent(
                objectType = XapiObjectType.Agent,
                account = XapiAccount(
                    name = route.groupId,
                    homePage = schoolSelfUrl.toString()
                )
            )

            schoolDataSource.xapiStatementsResource.getAsFlow(
                listParams = XapiStatementsResource.GetStatementParams(
                    verb = VERB_SAVED,
                    agent = groupAgent,
                ),
                dataLoadParams = DataLoadParams()
            ).collect { dataLoadState ->
                val statementResult = dataLoadState.dataOrNull()
                if (statementResult == null) {
                    Napier.d("loadGroupDetail: data not yet loaded, waiting")
                    return@collect
                }

                // Find the latest statement for this group by sorting by timestamp
                val groupStatement = statementResult.statements
                    .filter { statement ->
                        val group = statement.`object` as? XapiGroup
                        group?.account?.name == route.groupId
                    }
                    .maxByOrNull { it.timestamp ?: it.stored ?: Instant.DISTANT_PAST }

                if (groupStatement != null) {
                    val group = groupStatement.`object` as XapiGroup
                    val memberNames = group.member?.mapNotNull { agent ->
                        val name = agent.name
                        if (name == null) {
                            Napier.w("loadGroupDetail: member agent has no name, skipping")
                        }
                        name
                    } ?: emptyList()
                    val statementId = groupStatement.id
                    if (statementId == null) {
                        Napier.e("loadGroupDetail: Group statement id is null, skipping update")
                        return@collect
                    }
                    val groupName = group.name
                    if (groupName == null) {
                        Napier.e("loadGroupDetail: Group name is null, skipping update")
                        return@collect
                    }

                    val classActivityId = groupStatement.context?.contextActivities?.parent
                        ?.firstOrNull()?.id
                    val classId = classActivityId?.removePrefix("${schoolSelfUrl}${CLASS}")

                    _uiState.update { prev ->
                        prev.copy(
                            groupName = groupName,
                            groupMembers = memberNames,
                            statementGroupId = statementId.toString(),
                            classId = classId
                        )
                    }

                    _appUiState.update {
                        it.copy(
                            title = group.name?.asUiText()
                        )
                    }
                }
            }
        }
    }
}
