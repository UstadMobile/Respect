package world.respect.shared.viewmodel.clazz.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import world.respect.datalayer.oneroster.rostering.FakeRosterDataSource
import world.respect.datalayer.oneroster.rostering.model.OneRosterRoleEnum
import world.respect.datalayer.oneroster.rostering.model.OneRosterUser
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.first_name
import world.respect.shared.generated.resources.last_name
import world.respect.shared.generated.resources.all
import world.respect.shared.generated.resources.active
import world.respect.shared.navigation.AddPersonToClazz
import world.respect.shared.navigation.NavCommand
import world.respect.shared.util.FilterChipsOption
import world.respect.shared.util.SortOrderOption
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.clazz.detail.ClazzDetailViewModel.Companion.ALL
import world.respect.shared.viewmodel.clazz.detail.ClazzDetailViewModel.Companion.NAME

data class ClazzDetailUiState(
    val listOfTeachers: List<OneRosterUser> = emptyList(),
    val listOfStudents: List<OneRosterUser> = emptyList(),
    val listOfPending: List<OneRosterUser> = emptyList(),
    val sortOptions: List<SortOrderOption> = emptyList(),
    val selectedSortOption: String = NAME,
    val chipOptions: List<FilterChipsOption> = emptyList(),
    val selectedChip: String = ALL
)

class ClazzDetailViewModel(
    savedStateHandle: SavedStateHandle,
) : RespectViewModel(savedStateHandle) {

    private val fakeRosterDataSource = FakeRosterDataSource()
    private val _uiState = MutableStateFlow(ClazzDetailUiState())

    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val users = fakeRosterDataSource.getAllUsers()

            val teachers = users.filter { user ->
                user.enabledUser && user.roles.any { it.role == OneRosterRoleEnum.TEACHER }
            }

            val students = users.filter { user ->
                user.enabledUser && user.roles.any { it.role == OneRosterRoleEnum.STUDENT }
            }
            val pendingInvites = users.filter { user ->
                !user.enabledUser
            }

            _uiState.update {
                it.copy(
                    listOfTeachers = teachers,
                    listOfStudents = students,
                    listOfPending = pendingInvites,
                    sortOptions = listOf(
                        SortOrderOption(getString(Res.string.first_name)),
                        SortOrderOption(getString(Res.string.last_name))
                    ),
                    chipOptions = listOf(
                        FilterChipsOption(getString(Res.string.all)),
                        FilterChipsOption(getString(Res.string.active))
                    ),
                    selectedSortOption = getString(Res.string.first_name),
                )
            }
        }
    }

    fun onClickAddPersonToClazz() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                AddPersonToClazz
            )
        )
    }

    fun onClickSortOption(title: String) {
        _uiState.update { it.copy(selectedSortOption = title) }
    }

    fun onSelectChip(chip: String) {
        _uiState.update { it.copy(selectedChip = chip) }
    }

    companion object {
        const val NAME = "Name"
        const val ALL = "All"

    }
}
