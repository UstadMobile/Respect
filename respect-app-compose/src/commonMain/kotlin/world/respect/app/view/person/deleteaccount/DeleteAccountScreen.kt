package world.respect.app.view.person.deleteaccount

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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
import world.respect.shared.generated.resources.delete_headline
import world.respect.shared.generated.resources.delete_supporting_content
import world.respect.shared.generated.resources.enter_username
import world.respect.shared.generated.resources.name
import world.respect.shared.generated.resources.permanently_delete
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
        onEntityChanged = viewModel::onEntityChanged
    )

}

@Composable
fun DeleteAccountScreen(
    uiState: DeleteAccountUiState,
    onDeleteAccount: () -> Unit = {},
    onEntityChanged: (String) -> Unit = {}
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .defaultItemPadding()
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(Res.string.delete_headline),
            style = MaterialTheme.typography.titleSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "${stringResource(Res.string.enter_username)} " +
                    "(${uiState.userName}) " +
                    stringResource(Res.string.delete_supporting_content),
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(24.dp))


        OutlinedTextField(
            modifier = Modifier
                .testTag("name")
                .fillMaxWidth(),
            value = uiState.enteredName,
            label = { Text(stringResource(Res.string.name) + "*") },
            onValueChange = onEntityChanged,
            isError = uiState.userNameError != null,
            singleLine = true,
            supportingText = {
                uiState.userNameError?.let { errorText ->
                    Text(uiTextStringResource(errorText))
                }
            }
        )

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onDeleteAccount,
            enabled = uiState.userNameError == null && uiState.enteredName.isNotBlank()
        ) {
            Text(
                text = stringResource(Res.string.permanently_delete)
            )
        }
    }
}
