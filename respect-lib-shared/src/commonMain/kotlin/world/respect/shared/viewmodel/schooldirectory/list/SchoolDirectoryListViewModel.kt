package world.respect.shared.viewmodel.schooldirectory.list

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.clazz
import world.respect.shared.generated.resources.school_directory
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.SchoolDirectoryEdit
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.FabUiState

data class SchoolDirectoryListUIState(
    val items: List<String> = emptyList(),
)
class SchoolDirectoryListViewModel(
    private val respectAccountManager: RespectAccountManager,
    savedStateHandle: SavedStateHandle
) : RespectViewModel(savedStateHandle){

    private val _uiState = MutableStateFlow(SchoolDirectoryListUIState())
    val uiState = _uiState.asStateFlow()

    init {
        _appUiState.update {
            it.copy(
                title = Res.string.school_directory.asUiText(),
                hideBottomNavigation = true,
                fabState = it.fabState.copy(
                    icon = FabUiState.FabIcon.ADD,
                    text = Res.string.clazz.asUiText(),
                    onClick = ::onClickAdd
                )
            )
        }
    }

    fun onClickAdd() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(SchoolDirectoryEdit)
        )
    }
}
