package world.respect.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import world.respect.app.appstate.AppUiState
import world.respect.app.appstate.LoadingUiState

abstract class RespectViewModel() : ViewModel() {

    lateinit var navController: NavHostController
    protected val _appUiState = MutableStateFlow(AppUiState())
    val appUiState: Flow<AppUiState> = _appUiState.asStateFlow()

    /**
     * Shorthand to make it easier to update the loading state
     */
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