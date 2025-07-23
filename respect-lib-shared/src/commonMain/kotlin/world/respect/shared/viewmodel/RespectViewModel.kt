package world.respect.shared.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import io.ktor.http.Url
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import world.respect.datalayer.respect.model.RespectRealm
import world.respect.shared.domain.account.RespectAccount
import world.respect.shared.navigation.NavCommand
import world.respect.shared.viewmodel.app.appstate.AppUiState
import world.respect.shared.viewmodel.app.appstate.LoadingUiState

abstract class RespectViewModel(
    protected val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    protected val _appUiState = MutableStateFlow(AppUiState())

    open val appUiState: Flow<AppUiState> = _appUiState.asStateFlow()

    protected open val _navCommandFlow = MutableSharedFlow<NavCommand>(
        replay = 1,
        extraBufferCapacity = 0,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    open val navCommandFlow: Flow<NavCommand> = _navCommandFlow.asSharedFlow()

    //Placeholder: will be provided via an AccountManager that will use multiplatform settings.
    protected val activeAccount = RespectAccount(
        userSourcedId = "testacct",
        serverUrls = RespectRealm(
            xapi = Url("https://example.org/xapi"),
            oneRoster = Url("https://example.org/oneroster"),
            respectExt = Url("https://example.org/respect-ext"),
        )
    )

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