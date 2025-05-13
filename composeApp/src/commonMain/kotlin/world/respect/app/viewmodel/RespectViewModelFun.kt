package world.respect.app.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import kotlinx.coroutines.flow.map
import world.respect.app.appstate.AppUiState
import world.respect.app.effects.AppUiStateEffect
import kotlin.reflect.KClass

@Composable
fun <T : RespectViewModel> respectViewModel(
    modelClass: KClass<T>,
    appUiStateMap: ((AppUiState) -> AppUiState)? = null,
    onSetAppUiState: (AppUiState) -> Unit,
    navController: NavHostController,
    ): T {

    val viewModel= viewModel(modelClass)

    viewModel.navController=navController
    val uiStateFlow = remember(viewModel.appUiState, appUiStateMap) {
        if(appUiStateMap != null) {
            viewModel.appUiState.map(appUiStateMap)
        }else {
            viewModel.appUiState
        }
    }

    AppUiStateEffect(
        appUiStateFlow = uiStateFlow,
        onSetAppUiState = onSetAppUiState,
    )
    return viewModel
}

