package world.respect.app.view.person.deleteaccount

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import world.respect.shared.viewmodel.person.deleteaccount.DeleteAccountUiState
import world.respect.shared.viewmodel.person.deleteaccount.DeleteAccountViewModel


@Composable
fun DeleteAccountScreen(
    viewModel: DeleteAccountViewModel
) {

    val uiState by viewModel.uiState.collectAsState()

    DeleteAccountScreen(
        uiState = uiState,
        onDeleteAccount = viewModel::onDeleteAccount,
    )

}

@Composable
fun DeleteAccountScreen(
    uiState: DeleteAccountUiState,
    onDeleteAccount: () -> Unit = {},

    ) {

}
