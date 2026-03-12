package world.respect.app.view.apps.changehistory

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import world.respect.shared.viewmodel.apps.changehistory.ChangeHistoryUiState
import world.respect.shared.viewmodel.apps.changehistory.ChangeHistoryViewModel

@Composable
fun ChangeHistoryScreen(
    viewModel: ChangeHistoryViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()

    ChangeHistoryScreen(
        uiState = uiState
    )

}

@Composable
fun ChangeHistoryScreen(
    uiState: ChangeHistoryUiState,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {


    }

}