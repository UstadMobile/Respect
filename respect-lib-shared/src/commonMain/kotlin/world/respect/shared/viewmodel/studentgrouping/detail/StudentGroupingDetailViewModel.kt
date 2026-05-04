package world.respect.shared.viewmodel.studentgrouping.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.putJsonArray
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.DataLoadingState
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.ext.dataOrNull
import world.respect.datalayer.school.model.Clazz
import world.respect.datalayer.school.model.Clazz.Companion.GROUP_IDS
import world.respect.lib.xapi.model.VERB_VOIDED
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.create_group
import world.respect.shared.generated.resources.edit
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.NavResultReturner
import world.respect.shared.navigation.StudentGroupingDetail
import world.respect.shared.navigation.StudentGroupingEdit
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.lib.xapi.model.XapiAccount
import world.respect.lib.xapi.model.XapiAgent
import world.respect.lib.xapi.model.XapiGroup.Companion.RESULT_KEY_GROUP_UPDATED
import world.respect.lib.xapi.model.XapiObjectType
import world.respect.lib.xapi.model.XapiStatement
import world.respect.lib.xapi.model.XapiVerb
import world.respect.shared.viewmodel.app.appstate.FabUiState
import kotlin.getValue
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi


data class StudentGroupingDetailUiState(
    val groupName: String = "",
    val groupMembers: List<String> = emptyList(),
    val showDeleteGroupDialog: Boolean = false,
    val clazz: DataLoadState<Clazz> = DataLoadingState(),
)

class StudentGroupingDetailViewModel(
    savedStateHandle: SavedStateHandle,
    var respectAccountManager: RespectAccountManager,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = respectAccountManager.requireActiveAccountScope()

    private val _uiState = MutableStateFlow(StudentGroupingDetailUiState())

    val uiState = _uiState.asStateFlow()

    private val schoolDataSource: SchoolDataSource by inject()

    private val route: StudentGroupingDetail = savedStateHandle.toRoute()

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

        // Listen for result from edit screen and refresh
        val navResultReturner: NavResultReturner = getKoin().get()
        viewModelScope.launch {
            navResultReturner.filteredResultFlowForKey(
                RESULT_KEY_GROUP_UPDATED
            ).collect {
                loadGroupDetail()
            }
        }

        viewModelScope.launch {
            schoolDataSource.classDataSource.findByGuidAsFlow(route.classId)
                .collect { clazz ->
                    _uiState.update { it.copy(clazz = clazz) }
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
                val schoolSelfUrl = respectAccountManager.activeAccount?.school?.self

                val group = schoolDataSource.xapiActorDataSource.getGroupDetail(route.groupId)
                    ?: return@launch

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
                    display = mapOf("en" to VERB_VOIDED)
                )

                val statement = XapiStatement(
                    actor = actor,
                    verb = verb,
                    `object` = group,
                    timestamp = Clock.System.now()
                )

                schoolDataSource.xapiStatementsResource.post(listOf(statement))

                // Remove group ID from class metadata
                val clazz = _uiState.value.clazz.dataOrNull() ?: return@launch

                val existingGroupIds = clazz.metadata
                    ?.get(GROUP_IDS)
                    ?.jsonArray
                    ?.map { it.jsonPrimitive.content }
                    ?.filter { it != route.groupId }
                    ?: emptyList()

                val updatedMetadata = buildJsonObject {
                    clazz.metadata?.forEach { (key, value) ->
                        if (key != GROUP_IDS) put(key, value)
                    }
                    putJsonArray(GROUP_IDS) {
                        existingGroupIds.forEach { add(it) }
                    }
                }

                val updatedClazz = clazz.copy(
                    metadata = updatedMetadata,
                    lastModified = Clock.System.now()
                )

                schoolDataSource.classDataSource.store(listOf(updatedClazz))

                _navCommandFlow.tryEmit(NavCommand.PopUp())

            } catch (e: Throwable) {
                Napier.e("onConfirmDeleteGroup ERROR", throwable = e)
            }
        }
    }

    private fun loadGroupDetail() {
        viewModelScope.launch {
            try {
                val group = schoolDataSource.xapiActorDataSource.getGroupDetail(route.groupId)

                if (group != null) {
                    val memberNames = group.member?.mapNotNull { it.name } ?: emptyList()

                    _uiState.update { prev ->
                        prev.copy(
                            groupName = group.name ?: "",
                            groupMembers = memberNames
                        )
                    }
                }

                _appUiState.update {
                    it.copy(
                        title = group?.name?.asUiText() ?: Res.string.create_group.asUiText(),
                        userAccountIconVisible = false,
                        hideBottomNavigation = true,
                    )
                }
            } catch (e: Throwable) {
                Napier.e("loadGroupDetail ERROR", throwable = e)
            }
        }
    }
}
