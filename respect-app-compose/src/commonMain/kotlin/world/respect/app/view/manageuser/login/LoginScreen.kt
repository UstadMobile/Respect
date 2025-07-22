package world.respect.app.view.manageuser.login

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.defaultItemPadding
import world.respect.app.components.defaultScreenPadding
import world.respect.app.components.uiTextStringResource
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.login
import world.respect.shared.generated.resources.password_label
import world.respect.shared.generated.resources.userId_label
import world.respect.shared.viewmodel.manageuser.login.LoginUiState
import world.respect.shared.viewmodel.manageuser.login.LoginViewModel

@Composable
fun LoginScreen(
    viewModel: LoginViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    LoginScreen(
        uiState = uiState,
        onUserIdChanged = viewModel::onUserIdChanged,
        onPasswordChanged = viewModel::onPasswordChanged,
        onClickLogin = viewModel::onClickLogin
    )
}

@Composable
fun LoginScreen(
    uiState: LoginUiState,
    onUserIdChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onClickLogin: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .defaultScreenPadding()
    ) {
        OutlinedTextField(
            value = uiState.userId,
            onValueChange = onUserIdChanged,
            label = { Text(stringResource(Res.string.userId_label)) },
            placeholder = { Text(stringResource(Res.string.userId_label)) },
            singleLine = true,
            isError = uiState.userIdError != null,
            supportingText = uiState.userIdError?.let {
                { Text(uiTextStringResource(it)) }
            },
            modifier = Modifier.fillMaxWidth().defaultItemPadding()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = uiState.password,
            onValueChange = onPasswordChanged,
            label = { Text(stringResource(Res.string.password_label)) },
            placeholder = { Text(stringResource(Res.string.password_label)) },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            isError = uiState.passwordError != null,
            supportingText = uiState.passwordError?.let {
                { Text(uiTextStringResource(it)) }
            },
            modifier = Modifier.fillMaxWidth().defaultItemPadding()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onClickLogin,
            modifier = Modifier
                .fillMaxWidth().defaultItemPadding()
        ) {
            Text(text = stringResource(Res.string.login))
        }
    }
}
