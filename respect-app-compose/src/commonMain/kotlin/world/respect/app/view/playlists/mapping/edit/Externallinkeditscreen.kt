package world.respect.app.view.playlists.mapping.edit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.defaultItemPadding
import world.respect.app.components.uiTextStringResource
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.next
import world.respect.shared.viewmodel.playlists.mapping.edit.ExternalLinkEditUiState
import world.respect.shared.viewmodel.playlists.mapping.edit.ExternalLinkEditViewModel

@Composable
fun ExternalLinkEditScreenForViewModel(
    viewModel: ExternalLinkEditViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()
    ExternalLinkEditScreen(
        uiState = uiState,
        onUrlChanged = viewModel::onUrlChanged,
        onClickNext = viewModel::onClickNext,
    )
}

@Composable
fun ExternalLinkEditScreen(
    uiState: ExternalLinkEditUiState = ExternalLinkEditUiState(),
    onUrlChanged: (String) -> Unit = {},
    onClickNext: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 8.dp),
    ) {
        OutlinedTextField(
            value = uiState.url,
            onValueChange = onUrlChanged,
            isError = uiState.urlError != null,
            supportingText = uiState.urlError?.let { error ->
                { Text(text = uiTextStringResource(error)) }
            },
            modifier = Modifier
                .fillMaxWidth()
                .defaultItemPadding()
                .testTag("external_link_url_field"),
            singleLine = true,
        )

        Button(
            onClick = onClickNext,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .testTag("external_link_next_button"),
        ) {
            Text(text = stringResource(Res.string.next))
        }
    }
}