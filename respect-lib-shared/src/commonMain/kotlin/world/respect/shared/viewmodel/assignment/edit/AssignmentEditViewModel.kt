package world.respect.shared.viewmodel.assignment.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.DataLoadingState
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.RespectAppDataSource
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.ext.dataOrNull
import world.respect.datalayer.ext.isReadyAndSettled
import world.respect.datalayer.school.ClassDataSource
import world.respect.datalayer.school.model.Assignment
import world.respect.datalayer.school.model.AssignmentAssigneeRef
import world.respect.datalayer.school.model.AssignmentLearningUnitRef
import world.respect.datalayer.school.model.Clazz
import world.respect.lib.opds.model.OpdsPublication
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.school.SchoolPrimaryKeyGenerator
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

data class AssignmentEditUiState(
    val assignment: DataLoadState<Assignment> = DataLoadingState(),
    val assigneeText: String = "",
    val nameError: UiText? = null,
    val classOptions: List<Clazz> = emptyList(),
    val classError: UiText? = null,
    val learningUnitInfoFlow: (Url) -> Flow<DataLoadState<OpdsPublication>> = { flowOf(DataLoadingState()) },
) {
    val fieldsEnabled: Boolean
        get() = assignment.isReadyAndSettled()

    val hasErrors: Boolean
        get() = nameError != null || classError != null
}

class AssignmentEditViewModel(
    savedStateHandle: SavedStateHandle,
    accountManager: RespectAccountManager,
    private val json: Json,
    private val resultReturner: NavResultReturner,
    private val respectAppDataSource: RespectAppDataSource,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireSelectedAccountScope()

    private val route: AssignmentEdit = savedStateHandle.toRoute()

    private val schoolDataSource: SchoolDataSource by inject()

    private val _uiState = MutableStateFlow(AssignmentEditUiState())

    val uiState = _uiState.asStateFlow()

    private val debouncer = LaunchDebouncer(viewModelScope)

    private val schoolPrimaryKeyGenerator: SchoolPrimaryKeyGenerator by inject()

    private val uid = route.guid ?: schoolPrimaryKeyGenerator.primaryKeyGenerator.nextId(
        Assignment.TABLE_ID
    ).toString()

    private fun LearningUnitSelection.toRef(): AssignmentLearningUnitRef {
        return AssignmentLearningUnitRef(
            learningUnitManifestUrl = this.learningUnitManifestUrl,
            appManifestUrl = this.appManifestUrl,
        )
    }

    init {
        _appUiState.update { prev ->
            prev.copy(
                title = if(route.guid == null) {
                    Res.string.add_assignment.asUiText()
                }else {
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

            if(route.guid != null) {
                loadEntity(
                    json = json,
                    serializer = Assignment.serializer(),
                    loadFn = { params ->
                        schoolDataSource.assignmentDataSource.findByGuid(
                            params, route.guid
                        )
                    },
                    uiUpdateFn = { entity ->
                        _uiState.update { prev ->
                            val assigneeClassUid = entity.dataOrNull()?.assignees?.firstOrNull()?.uid
                            prev.copy(
                                assignment = entity,
                                assigneeText = classes.firstOrNull {
                                    it.guid == assigneeClassUid
                                }?.title ?: ""
                            )
                        }
                    }
                )
            }else {
                _uiState.update { prev ->
                    prev.copy(
                        assignment = DataReadyState(
                            Assignment(
                                uid = uid,
                                title = "",
                                description = "",
                                learningUnits = route.learningUnitSelected?.let {
                                    listOf(it.toRef())
                                } ?: emptyList()
                            )
                        )
                    )
                }
            }

            viewModelScope.launch {
                resultReturner.filteredResultFlowForKey(KEY_LEARNING_UNIT).collect { result ->
                    val learningUnit = result.result as? LearningUnitSelection ?: return@collect
                    val assignmentResourceRef = learningUnit.toRef()

                    _uiState.update { prev ->
                        val prevAssignment = prev.assignment.dataOrNull() ?: return@update prev

                        prev.copy(
                            assignment = DataReadyState(
                                data = prevAssignment.copy(
                                    learningUnits = prevAssignment.learningUnits + assignmentResourceRef
                                )
                            )
                        )
                    }
                }
            }
        }
    }

    fun learningUnitInfoFlowFor(url: Url): Flow<DataLoadState<OpdsPublication>> {
        return respectAppDataSource.opdsDataSource.loadOpdsPublication(
            url = url, params = DataLoadParams(), null, null
        )
    }

    fun onAssigneeClassSelected(clazz: Clazz) {
        val assignment = _uiState.value.assignment.dataOrNull() ?: return
        _uiState.update {
            it.copy(
                assignment = DataReadyState(
                    assignment.copy(
                        assignees = listOf(
                            AssignmentAssigneeRef(uid = clazz.guid)
                        )
                    )
                ),
                assigneeText = clazz.title,
                classError = null,
            )
        }
    }

    fun onEntityChanged(assignment: Assignment) {
        _uiState.update { prev ->
            prev.copy(
                assignment = DataReadyState(assignment),
                nameError = prev.nameError?.takeIf {
                    prev.assignment.dataOrNull()?.title == assignment.title
                },
            )
        }

        debouncer.launch(DEFAULT_SAVED_STATE_KEY) {
            savedStateHandle[DEFAULT_SAVED_STATE_KEY] = json.encodeToString(assignment)
        }
    }

    fun onAssigneeTextChanged(text: String) {
        _uiState.update {
            it.copy(assigneeText = text, classError = null)
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
        val assignment = uiState.value.assignment.dataOrNull() ?: return

        _uiState.update { prev ->
            prev.copy(
                assignment = DataReadyState(
                    data = assignment.copy(
                        learningUnits = assignment.learningUnits.filter {
                            it.learningUnitManifestUrl != ref.learningUnitManifestUrl
                        }
                    )
                )
            )
        }
    }

    fun onClickSave() {
        val stateToSave = _uiState.updateAndGet { prev ->
            val assignmentVal = prev.assignment.dataOrNull()

            prev.copy(
                nameError = Res.string.required_field.asUiText().takeIf {
                    assignmentVal?.title.isNullOrBlank()
                },
                classError = Res.string.required_field.asUiText().takeIf {
                    assignmentVal?.assignees?.isEmpty() != false
                }
            )
        }

        if(stateToSave.hasErrors)
            return

        val assignment = uiState.value.assignment.dataOrNull() ?: return

        launchWithLoadingIndicator {
            schoolDataSource.assignmentDataSource.store(
                listOf(assignment.copy(lastModified = Clock.System.now()))
            )

            if(route.guid == null) {
                _navCommandFlow.tryEmit(
                    NavCommand.Navigate(
                        destination = AssignmentDetail(uid = uid),
                        popUpTo = route,
                        popUpToInclusive = true,
                    )
                )
            }else {
                _navCommandFlow.tryEmit(NavCommand.PopUp())
            }
        }
    }

    companion object {

        const val KEY_LEARNING_UNIT = "result_learning_unit"

    }

}