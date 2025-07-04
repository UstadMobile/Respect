package world.respect.app.viewmodel

import androidx.compose.runtime.Composable
import org.koin.compose.viewmodel.koinViewModel
import world.respect.app.appstate.AppUiState
import world.respect.app.effects.AppUiStateEffect
import world.respect.navigation.NavCommandEffect
import world.respect.navigation.RespectComposeNavController

@Composable
inline fun <reified T : RespectViewModel> respectViewModel(
    noinline onSetAppUiState: (AppUiState) -> Unit,
    navController: RespectComposeNavController,
): T {

    val viewModel: T = koinViewModel()

    AppUiStateEffect(
        appUiStateFlow = viewModel.appUiState,
        onSetAppUiState = onSetAppUiState,
    )

    NavCommandEffect(
        navHostController = navController,
        navCommandFlow = viewModel.navCommandFlow
    )

    return viewModel
}

