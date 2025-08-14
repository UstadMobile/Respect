package world.respect.shared.viewmodel.manageuser.howpasskeywork

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.how_passkey_works
import world.respect.shared.viewmodel.RespectViewModel


class HowPasskeyWorksViewModel(
    savedStateHandle: SavedStateHandle,
) : RespectViewModel(savedStateHandle) {
    init {
        viewModelScope.launch {
            _appUiState.update { prev ->
                prev.copy(
                    title = getString(Res.string.how_passkey_works),
                    hideBottomNavigation = true,
                    userAccountIconVisible = false,
                    showBackButton = true
                )
            }
        }
    }
}
