package world.respect.app.view.apps.enterlink

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import org.jetbrains.compose.resources.stringResource
import respect.composeapp.generated.resources.*
import world.respect.app.viewmodel.apps.enterlink.EnterLinkViewModel

@Composable
fun EnterLinkScreen(
    viewModel: EnterLinkViewModel,
    navController: NavHostController,
) {

    var link by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(Res.string.app_link_provided_message),
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = link,
            onValueChange = {
                link = it
                if (uiState.isError) {
                    viewModel.onButtonClick(it)
                }
            },
            label = {
                Text(
                    text = stringResource(Res.string.link_label),
                    style = MaterialTheme.typography.labelLarge
                )
            },
            placeholder = {
                Text(
                    text = stringResource(Res.string.example_url_placeholder),
                    style = MaterialTheme.typography.bodySmall
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.isError
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.onButtonClick(link)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(Res.string.next),
                style = MaterialTheme.typography.labelLarge
            )
        }

        if (uiState.isError) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(Res.string.error_link_message),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
