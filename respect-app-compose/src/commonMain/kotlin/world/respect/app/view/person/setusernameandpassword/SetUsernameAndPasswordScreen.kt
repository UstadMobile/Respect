package world.respect.app.view.person.setusernameandpassword

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.utf16CodePoint
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.RespectPasswordField
import world.respect.app.components.defaultItemPadding
import world.respect.app.components.uiTextStringResource
import world.respect.shared.domain.account.username.validateusername.ValidateUsernameUseCase
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.password_label
import world.respect.shared.generated.resources.username_label
import world.respect.shared.util.ext.isLoading
import world.respect.shared.viewmodel.app.appstate.AppUiState
import world.respect.shared.viewmodel.person.setusernameandpassword.SetUsernameAndPasswordUiState
import world.respect.shared.viewmodel.person.setusernameandpassword.SetUsernameAndPasswordViewModel

@Composable
fun SetUsernameAndPasswordScreen(
    viewModel: SetUsernameAndPasswordViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()
    val appUiState by viewModel.appUiState.collectAsState()

    SetUsernameAndPasswordScreen(
        uiState = uiState,
        appUiState = appUiState,
        onUsernameChanged = viewModel::onUsernameChanged,
        onPasswordChanged = viewModel::onPasswordChanged,
    )
}

@Composable
fun SetUsernameAndPasswordScreen(
    uiState: SetUsernameAndPasswordUiState,
    appUiState: AppUiState,
    onUsernameChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
) {

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = uiState.username,
            onValueChange = onUsernameChanged,
            label = { Text(stringResource(Res.string.username_label)) },
            singleLine = true,
            isError = uiState.usernameErr != null,
            supportingText = uiState.usernameErr?.let {
                { Text(uiTextStringResource(it)) }
            },
            enabled = !appUiState.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("username")
                .defaultItemPadding()
                .onKeyEvent { keyEvent ->
                    if (keyEvent.type == KeyEventType.KeyDown) {
                        !ValidateUsernameUseCase.isValidUsernameChar(
                            keyEvent.utf16CodePoint.toChar()
                        )
                    } else false
                }
        )

        RespectPasswordField(
            value = uiState.password,
            onValueChange = onPasswordChanged,
            label = { Text(stringResource(Res.string.password_label)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            isError = uiState.passwordErr != null,
            supportingText = uiState.passwordErr?.let {
                { Text(uiTextStringResource(it)) }
            },
            enabled = !appUiState.isLoading,
            modifier = Modifier.fillMaxWidth().defaultItemPadding().testTag("password")
        )
    }

}

