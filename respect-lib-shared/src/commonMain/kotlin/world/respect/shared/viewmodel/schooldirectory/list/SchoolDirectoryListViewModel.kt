package world.respect.shared.viewmodel.schooldirectory.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import world.respect.datalayer.RespectAppDataSource
import world.respect.datalayer.respect.model.RespectSchoolDirectory
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.school_directories
import world.respect.shared.generated.resources.school_directory
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.SchoolDirectoryEdit
import world.respect.shared.navigation.SchoolDirectoryList
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.FabUiState

data class SchoolDirectoryListUiState(
    val schoolDirectory: List<RespectSchoolDirectory> = emptyList(),
)

class SchoolDirectoryListViewModel(
    savedStateHandle: SavedStateHandle,
    private val respectAppDataSource: RespectAppDataSource,
) : RespectViewModel(savedStateHandle) {
    private val _uiState = MutableStateFlow(SchoolDirectoryListUiState())

    val uiState = _uiState.asStateFlow()

    init {
        _appUiState.update {
            it.copy(
                title = Res.string.school_directories.asUiText(),
                hideBottomNavigation = true,
                fabState = it.fabState.copy(
                    icon = FabUiState.FabIcon.ADD,
                    text = Res.string.school_directory.asUiText(),
                    onClick = ::onClickAdd,
                    visible = true
                )
            )
        }

        viewModelScope.launch {
            respectAppDataSource.schoolDirectoryDataSource.allDirectoriesAsFlow().collect {
                _uiState.update { prev ->
                    prev.copy(schoolDirectory = it)
                }
            }
        }
    }

    fun onClickAdd() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                destination = SchoolDirectoryEdit,
                popUpTo = SchoolDirectoryList
            )
        )
    }

    fun onDeleteDirectory(directory: RespectSchoolDirectory) {
        viewModelScope.launch {
            respectAppDataSource.schoolDirectoryDataSource.deleteDirectory(directory)
        }
    }
}
