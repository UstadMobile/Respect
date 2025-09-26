package world.respect.shared.viewmodel.settings

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.settings
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.SchoolDirectoryList
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel

data class SettingUIState(
    val items: List<String> = emptyList(),
)

class SettingViewModel(
    private val respectAccountManager: RespectAccountManager,
    savedStateHandle: SavedStateHandle
) : RespectViewModel(savedStateHandle) {

    private val _uiState = MutableStateFlow(SettingUIState())

    val uiState = _uiState.asStateFlow()

    init {
        _appUiState.update {
            it.copy(
                title = Res.string.settings.asUiText(),
                hideBottomNavigation = true,
                userAccountIconVisible = false,
            )
        }
    }

    fun onClickSchoolDirectory() {
        _navCommandFlow.tryEmit(NavCommand.Navigate(SchoolDirectoryList))

    }

}
