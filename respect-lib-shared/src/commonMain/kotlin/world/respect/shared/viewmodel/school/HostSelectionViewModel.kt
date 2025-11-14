package world.respect.shared.viewmodel.school

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import world.respect.datalayer.RespectAppDataSource
import world.respect.datalayer.respect.model.RespectSchoolDirectory
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.select_host
import world.respect.shared.navigation.AddSchool
import world.respect.shared.navigation.NavCommand
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel

data class HostSelectionListUiState(
    val schoolDirectory: List<RespectSchoolDirectory> = emptyList(),
)

class HostSelectionViewModel(
    savedStateHandle: SavedStateHandle,
    private val respectAppDataSource: RespectAppDataSource,
) : RespectViewModel(savedStateHandle) {
    private val _uiState = MutableStateFlow(HostSelectionListUiState())

    val uiState = _uiState.asStateFlow()

    init {
        _appUiState.update {
            it.copy(
                title = Res.string.select_host.asUiText(),
                hideBottomNavigation = true,
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

    fun onClickNext(directory: RespectSchoolDirectory) {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                AddSchool.create(directory.baseUrl)
            )
        )
    }
}