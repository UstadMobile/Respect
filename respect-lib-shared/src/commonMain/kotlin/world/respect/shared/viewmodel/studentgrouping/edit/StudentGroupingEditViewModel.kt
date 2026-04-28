package world.respect.shared.viewmodel.studentgrouping.edit

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
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.DataLoadingState
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.db.school.ext.fullName
import world.respect.datalayer.ext.dataOrNull
import world.respect.datalayer.school.PersonDataSource
import world.respect.datalayer.school.model.Clazz
import world.respect.datalayer.school.model.Clazz.Companion.GROUP_IDS
import world.respect.datalayer.school.model.EnrollmentRoleEnum
import world.respect.datalayer.school.model.Person
import world.respect.datalayer.school.xapi.model.VERB_CREATED
import world.respect.datalayer.school.xapi.model.XapiAccount
import world.respect.datalayer.school.xapi.model.XapiAgent
import world.respect.datalayer.school.xapi.model.XapiGroup
import world.respect.datalayer.school.xapi.model.XapiGroup.Companion.RESULT_KEY_GROUP_UPDATED
import world.respect.datalayer.school.xapi.model.XapiObjectType
import world.respect.datalayer.school.xapi.model.XapiStatement
import world.respect.datalayer.school.xapi.model.XapiVerb
import world.respect.datalayer.shared.paging.EmptyPagingSourceFactory
import world.respect.datalayer.shared.paging.IPagingSourceFactory
import world.respect.datalayer.shared.paging.PagingSourceFactoryHolder
import world.respect.libutil.util.time.localDateInCurrentTimeZone
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.account.RespectSessionAndPerson
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.edit_group
import world.respect.shared.generated.resources.create_group
import world.respect.shared.generated.resources.required_field
import world.respect.shared.generated.resources.save
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.StudentGroupingDetail
import world.respect.shared.navigation.StudentGroupingEdit
import world.respect.shared.resources.UiText
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.ActionBarButtonUiState
import kotlin.getValue
import kotlin.time.Clock
import kotlin.toString
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class StudentGroupingEditUiState(
    val selectedAccount: RespectSessionAndPerson? = null,
    val groupName: String = "",
    val groupNameError: UiText? = null,
    val students: IPagingSourceFactory<Int, Person> = EmptyPagingSourceFactory(),
    val selectedStudentIds: List<String> = emptyList(),
    val selectedStudentNames: List<String> = emptyList(),
    val personName: String = "",
    val clazz: DataLoadState<Clazz> = DataLoadingState(),

    )
class StudentGroupingEditViewModel(
    savedStateHandle: SavedStateHandle,
    var respectAccountManager: RespectAccountManager,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {


    override val scope: Scope = respectAccountManager.requireActiveAccountScope()

    private val _uiState = MutableStateFlow(StudentGroupingEditUiState())
    val uiState = _uiState.asStateFlow()

    private val schoolDataSource: SchoolDataSource by inject()
    private val route: StudentGroupingEdit = savedStateHandle.toRoute()

    private fun pagingSourceByRole(): PagingSourceFactoryHolder<Int, Person> {
        return PagingSourceFactoryHolder {
            schoolDataSource.personDataSource.listAsPagingSource(
                loadParams = DataLoadParams(),
                params = PersonDataSource.GetListParams(
                    filterByClazzUid = route.classUid,
                    filterByEnrolmentRole = EnrollmentRoleEnum.STUDENT,
                    inClassOnDay = localDateInCurrentTimeZone(),
                )
            )
        }
    }

    private val studentPagingSource = pagingSourceByRole()

    init {
        _appUiState.update {
            it.copy(
                title = if (route.groupId == null)
                    Res.string.create_group.asUiText()
                else
                    Res.string.edit_group.asUiText(),

                userAccountIconVisible = false,
                hideBottomNavigation = true,
                actionBarButtonState = ActionBarButtonUiState(
                    visible = true,
                    text = Res.string.save.asUiText(),
                    onClick = ::onClickSave
                ),
            )
        }

        _uiState.update {
            it.copy(students = studentPagingSource)
        }
        route.groupId?.let { groupId ->
            viewModelScope.launch {
                try {
                    val group = schoolDataSource.xapiActorDataSource.getGroupDetail(groupId) ?: return@launch

                    val memberNames = group.member
                        ?.mapNotNull { it.name }
                        ?: emptyList()


                    _uiState.update { prev ->
                        prev.copy(
                            groupName = group.name ?: "",
                            selectedStudentNames = memberNames,
                        )
                    }

                } catch (e: Throwable) {
                    Napier.e("EDIT_GROUP: ERROR loading group", throwable = e)
                }
            }
        }

        viewModelScope.launch {
            respectAccountManager.selectedAccountAndPersonFlow.collect { selectedAccount ->
                _uiState.update {
                    it.copy(
                        personName = selectedAccount?.person?.fullName() ?: ""
                    )
                }
            }
        }

        viewModelScope.launch {
            schoolDataSource.classDataSource.findByGuidAsFlow(route.classUid)
                .collect { clazz ->
                    _uiState.update { it.copy(clazz = clazz) }
                }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun onClickSave() {

        val schoolSelfUrl = respectAccountManager.activeAccount?.school?.self
        val groupName = _uiState.value.groupName

        if (groupName.isBlank()) {
            _uiState.update {
                it.copy(groupNameError = Res.string.required_field.asUiText())
            }
            return
        } else {
            _uiState.update { it.copy(groupNameError = null) }
        }

        viewModelScope.launch {
            try {
                val members = _uiState.value.selectedStudentNames.map { studentName ->
                    XapiAgent(
                        name = studentName,
                        objectType = XapiObjectType.Agent,
                        account = XapiAccount(
                            name = studentName,
                            homePage = schoolSelfUrl.toString()
                        )
                    )
                }

                val groupId = route.groupId ?: Uuid.random().toString()

                val group = XapiGroup(
                    objectType = XapiObjectType.Group,
                    name = groupName,
                    account = XapiAccount(
                        name = groupId,
                        homePage = schoolSelfUrl.toString()
                    ),
                    member = members
                )

                val actor = XapiAgent(
                    name = _uiState.value.personName,
                    objectType = XapiObjectType.Agent,
                    account = XapiAccount(
                        name = respectAccountManager.activeAccount?.userGuid ?: "",
                        homePage = schoolSelfUrl.toString()
                    )
                )

                val verb = XapiVerb(
                    id = VERB_CREATED,
                    display = mapOf("en" to VERB_CREATED)
                )

                val statement = XapiStatement(
                    actor = actor,
                    verb = verb,
                    `object` = group,
                    timestamp = Clock.System.now()
                )

                schoolDataSource.xapiStatementDataSource.store(listOf(statement))

                val clazz = _uiState.value.clazz.dataOrNull() ?: return@launch

                val existingGroupIds = clazz.metadata
                    ?.get(GROUP_IDS)
                    ?.jsonArray
                    ?.map { it.jsonPrimitive.content }
                    ?.toMutableList()
                    ?: mutableListOf()

                if (route.groupId == null && !existingGroupIds.contains(groupId)) {
                    existingGroupIds.add(groupId)
                }

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

                if (route.groupId == null) {
                    _navCommandFlow.tryEmit(
                        NavCommand.Navigate(
                            StudentGroupingDetail(groupId, route.classUid),
                            popUpTo = route,
                            popUpToInclusive = true
                        )
                    )
                } else {
                    sendResultAndPop(RESULT_KEY_GROUP_UPDATED, true)
                }

            } catch (e: Throwable) {
                Napier.e("onClickSave ERROR", throwable = e)
            }
        }
    }

    fun onStudentCheckedChange(person: Person, isChecked: Boolean) {
        _uiState.update { prev ->
            val name = person.fullName()
            val updatedNames = if (isChecked) {
                prev.selectedStudentNames + name
            } else {
                prev.selectedStudentNames - name
            }

            prev.copy(
                selectedStudentNames = updatedNames
            )
        }
    }

    fun onGroupNameChanged(name: String) {
        _uiState.update {
            it.copy(
                groupName = name,
                groupNameError = null
            )
        }
    }
}