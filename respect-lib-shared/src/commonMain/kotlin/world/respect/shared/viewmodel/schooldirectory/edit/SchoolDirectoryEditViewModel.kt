package world.respect.shared.viewmodel.schooldirectory.edit

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.add_directory
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel

data class SchoolDirectoryEditUIState(
    val items: List<String> = emptyList(),
)
class SchoolDirectoryEditViewModel(
    private val respectAccountManager: RespectAccountManager,
    savedStateHandle: SavedStateHandle
) : RespectViewModel(savedStateHandle){

    private val _uiState = MutableStateFlow(SchoolDirectoryEditUIState())
    val uiState = _uiState.asStateFlow()
    init {
        _appUiState.update {
            it.copy(
                title = Res.string.add_directory.asUiText(),
                hideBottomNavigation = true,
            )
        }
    }

}
