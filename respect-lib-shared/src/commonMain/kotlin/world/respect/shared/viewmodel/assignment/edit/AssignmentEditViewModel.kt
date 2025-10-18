package world.respect.shared.viewmodel.assignment.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.DataLoadingState
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.ext.dataOrNull
import world.respect.datalayer.ext.isReadyAndSettled
import world.respect.datalayer.school.ClassDataSource
import world.respect.datalayer.school.model.Assignment
import world.respect.datalayer.school.model.Clazz
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.school.SchoolPrimaryKeyGenerator
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.add_assignment
import world.respect.shared.generated.resources.edit_assignment
import world.respect.shared.generated.resources.save
import world.respect.shared.navigation.AssignmentEdit
import world.respect.shared.resources.UiText
import world.respect.shared.util.LaunchDebouncer
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.ActionBarButtonUiState

data class AssignmentEditUiState(
    val assignment: DataLoadState<Assignment> = DataLoadingState(),
    val assigneeText: String = "",
    val nameError: UiText? = null,
    val classOptions: List<Clazz> = emptyList(),
) {
    val fieldsEnabled: Boolean
        get() = assignment.isReadyAndSettled()
}

class AssignmentEditViewModel(
    savedStateHandle: SavedStateHandle,
    accountManager: RespectAccountManager,
    private val json: Json,
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
                it.copy(classOptions = classes)
            }

            if(route.guid != null) {
                loadEntity(
                    json = json,
                    serializer = Assignment.serializer(),
                    loadFn = { params ->
                        schoolDataSource.assignmentDataSource.findByGuid(params, route.guid)
                    },
                    uiUpdateFn = {
                        _uiState.update { prev -> prev.copy(assignment = it) }
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
                            )
                        )
                    )
                }
            }
        }
    }

    fun onEntityChanged(assignment: Assignment) {
        _uiState.update { prev ->
            prev.copy(
                assignment = DataReadyState(assignment),
                nameError = prev.nameError?.takeIf {
                    prev.assignment.dataOrNull()?.title == assignment.title
                }
            )
        }

        debouncer.launch(DEFAULT_SAVED_STATE_KEY) {
            savedStateHandle[DEFAULT_SAVED_STATE_KEY] = json.encodeToString(assignment)
        }
    }

    fun onAssigneeTextChanged(text: String) {
        _uiState.update {
            it.copy(assigneeText = text)
        }
    }


    fun onClickSave() {

    }

}