package world.respect.app.view.shareddevicelogin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import world.respect.shared.viewmodel.sharedschooldevicelogin.EnterRollNumberUiState
import world.respect.shared.viewmodel.sharedschooldevicelogin.EnterRollNumberViewModel


@Composable
fun EnterRollNumberScreen (
    viewModel: EnterRollNumberViewModel
){
    val uiState: EnterRollNumberUiState by viewModel.uiState.collectAsState(
        EnterRollNumberUiState()
    )

}