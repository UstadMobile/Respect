package world.respect.app.view.testing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.send_db_upload_complete
import world.respect.shared.generated.resources.send_db_uploading
import world.respect.shared.generated.resources.something_went_wrong
import world.respect.shared.viewmodel.testing.SendDbToServerUiState
import world.respect.shared.viewmodel.testing.SendDbToServerViewModel

@Composable
fun SendDbToServerScreen(viewModel: SendDbToServerViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    SendDbToServerScreen(uiState)
}

@Composable
fun SendDbToServerScreen(uiState: SendDbToServerUiState) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator()
                Text(
                    text = stringResource(Res.string.send_db_uploading),
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .testTag("db_progress"),
                )
            }
            uiState.errorMessage != null -> {
                Text(
                    text = uiState.errorMessage?:stringResource(Res.string.something_went_wrong),
                    modifier = Modifier.testTag("db_failed"),
                )
            }
            else -> {
                Text(
                    text = stringResource(Res.string.send_db_upload_complete),
                    modifier = Modifier.testTag("db_completed"),
                )
            }
        }
    }
}
