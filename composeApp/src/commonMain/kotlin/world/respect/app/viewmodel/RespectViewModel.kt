package world.respect.app.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import world.respect.app.appstate.AppUiState
import world.respect.app.appstate.LoadingUiState
import world.respect.app.domain.account.RespectAccount
import world.respect.navigation.NavCommand

abstract class RespectViewModel(
    protected val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    protected val _appUiState = MutableStateFlow(AppUiState())

    val appUiState: Flow<AppUiState> = _appUiState.asStateFlow()

    protected val _navCommandFlow = MutableSharedFlow<NavCommand>(
        replay = 1,
        extraBufferCapacity = 0,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    val navCommandFlow: Flow<NavCommand> = _navCommandFlow.asSharedFlow()

    //Placeholder: will be provided via an AccountManager that will use multiplatform settings.
    protected val activeAccount = RespectAccount("testacct")

    /**
     * Shorthand to make it easier to update the loading state
     */
    @Suppress("unused")//reserved for future use
    protected var loadingState: LoadingUiState
        get() = _appUiState.value.loadingState
        set(value) {
            _appUiState.update {
                it.copy(loadingState = value)
            }
        }

    /**
     * Shorthand to set the title
     */
    protected var title: String?
        get() = _appUiState.value.title
        set(value) {
            _appUiState.update {
                it.copy(title = value)
            }
        }

}