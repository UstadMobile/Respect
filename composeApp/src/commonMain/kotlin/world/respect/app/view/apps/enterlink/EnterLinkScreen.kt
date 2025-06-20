package world.respect.app.view.apps.enterlink

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.ustadmobile.libuicompose.theme.md_theme_light_primaryContainer
import org.jetbrains.compose.resources.stringResource
import respect.composeapp.generated.resources.Res
import respect.composeapp.generated.resources.app_link_provided_message
import respect.composeapp.generated.resources.error_link_message
import respect.composeapp.generated.resources.link_label
import respect.composeapp.generated.resources.example_url_placeholder
import respect.composeapp.generated.resources.next
import world.respect.app.viewmodel.apps.enterlink.EnterLinkViewModel


@Composable
fun EnterLinkScreen(
    viewModel: EnterLinkViewModel,
    navController: NavHostController,
    ) {
    var link by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    var showError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(stringResource(Res.string.app_link_provided_message), fontSize = 16.sp)

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = link,
            onValueChange = {
                link = it
                if (showError) showError = false // reset error on new input
            },
            label = { Text(stringResource(Res.string.link_label)) },
            placeholder = { Text(stringResource(Res.string.example_url_placeholder)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color.Black,
                unfocusedBorderColor = Color.Black,
                focusedLabelColor = Color.Black,
                unfocusedLabelColor = Color.Black,
                disabledBorderColor = Color.Black,
                disabledLabelColor = Color.Black,
                cursorColor = Color.Black
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                showError = !viewModel.isValidUrl(link)
                if (!showError) {
                    // Proceed to next step
                }
            },
            colors = ButtonDefaults.buttonColors(
                md_theme_light_primaryContainer,
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(Res.string.next))
        }

        if (showError) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(Res.string.error_link_message),
                color = Color.Red,
                fontSize = 14.sp
            )
        }
    }
}
