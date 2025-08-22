package world.respect.shared.viewmodel.clazz.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import world.respect.datalayer.oneroster.rostering.OneRosterRosterDataSource
import world.respect.datalayer.oneroster.rostering.model.OneRosterClass
import world.respect.datalayer.oneroster.rostering.model.OneRosterRoleEnum
import world.respect.datalayer.oneroster.rostering.model.OneRosterUser
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.first_name
import world.respect.shared.generated.resources.last_name
import world.respect.shared.generated.resources.all
import world.respect.shared.generated.resources.active
import world.respect.shared.generated.resources.edit
import world.respect.shared.navigation.ClazzEdit
import world.respect.shared.navigation.AddPersonToClazz
import world.respect.shared.navigation.ClazzDetail
import world.respect.shared.navigation.NavCommand
import world.respect.shared.util.FilterChipsOption
import world.respect.shared.util.SortOrderOption
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.FabUiState
import world.respect.shared.viewmodel.clazz.detail.ClazzDetailViewModel.Companion.ALL

data class ClazzDetailUiState(
    val listOfTeachers: List<OneRosterUser> = emptyList(),
    val listOfStudents: List<OneRosterUser> = emptyList(),
    val listOfPending: List<OneRosterUser> = emptyList(),
    val chipOptions: List<FilterChipsOption> = emptyList(),
    val selectedChip: String = ALL,
    val sortOptions: List<SortOrderOption> = emptyList(),
    val activeSortOrderOption: SortOrderOption = SortOrderOption(
        Res.string.first_name, 1, true
    ),
    val fieldsEnabled: Boolean = true,
    val clazzDetail: OneRosterClass? = null,
    val isPendingExpanded: Boolean = true,
    val isTeachersExpanded: Boolean = true,
    val isStudentsExpanded: Boolean = true
)

class ClazzDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val oneRosterDataSource: OneRosterRosterDataSource,
) : RespectViewModel(savedStateHandle) {

    private val _uiState = MutableStateFlow(ClazzDetailUiState())

    val uiState = _uiState.asStateFlow()

    private val route: ClazzDetail = savedStateHandle.toRoute()

    init {
        viewModelScope.launch {
            val users = oneRosterDataSource.getAllUsers()

            val clazzDetail = oneRosterDataSource.getClassBySourcedId(route.sourcedId)

            val teachers = users.filter { user ->
                user.enabledUser && user.roles.any { it.role == OneRosterRoleEnum.TEACHER }
            }

            val students = users.filter { user ->
                user.enabledUser && user.roles.any { it.role == OneRosterRoleEnum.STUDENT }
            }
            val pendingInvites = users.filter { user ->
                !user.enabledUser
            }

            _appUiState.update {
                it.copy(
                    title = clazzDetail.title.asUiText(),
                    showBackButton = false,
                    fabState = FabUiState(
                        visible = true,
                        icon = FabUiState.FabIcon.EDIT,
                        text = Res.string.edit.asUiText(),
                        onClick = {
                            _navCommandFlow.tryEmit(
                                NavCommand.Navigate(
                                    ClazzEdit.create(
                                        route.sourcedId,
                                        modeEdit = true
                                    )
                                )
                            )
                        }
                    )
                )
            }


            _uiState.update {
                val sortOptions = listOf(
                    SortOrderOption(
                        fieldMessageId = Res.string.first_name,
                        flag = 1,
                        order = true
                    ),
                    SortOrderOption(
                        fieldMessageId = Res.string.last_name,
                        flag = 2,
                        order = true
                    )
                )
                it.copy(
                    listOfTeachers = teachers,
                    listOfStudents = students,
                    listOfPending = pendingInvites,
                    sortOptions = sortOptions,
                    activeSortOrderOption = sortOptions.first(),
                    chipOptions = listOf(
                        FilterChipsOption(getString(Res.string.all)),
                        FilterChipsOption(getString(Res.string.active))
                    ),
                    clazzDetail = clazzDetail
                )
            }
        }
    }

    fun onClickAddPersonToClazz(roleType: OneRosterRoleEnum) {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                AddPersonToClazz.create(roleType)
            )
        )
    }

    fun onSortOrderChanged(sortOption: SortOrderOption) {
        _uiState.update {
            it.copy(activeSortOrderOption = sortOption)
        }
    }

    fun onSelectChip(chip: String) {
        _uiState.update { it.copy(selectedChip = chip) }
    }

    fun onClickAcceptInvite(user: OneRosterUser) {}

    fun onClickDismissInvite(user: OneRosterUser) {}


    fun onTogglePendingSection() {
        _uiState.update { it.copy(isPendingExpanded = !it.isPendingExpanded) }
    }

    fun onToggleTeachersSection() {
        _uiState.update { it.copy(isTeachersExpanded = !it.isTeachersExpanded) }
    }

    fun onToggleStudentsSection() {
        _uiState.update { it.copy(isStudentsExpanded = !it.isStudentsExpanded) }
    }

    companion object {
        const val ALL = "All"

    }
}
