package world.respect.app.view.person.setusernameandpassword

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.utf16CodePoint
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.defaultItemPadding
import world.respect.app.components.uiTextStringResource
import world.respect.images.RespectImage
import world.respect.images.respectImagePainter
import world.respect.shared.domain.account.username.validateusername.ValidateUsernameUseCase
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.assign_qr_code_badge
import world.respect.shared.generated.resources.learn_more
import world.respect.shared.generated.resources.qr_code_badge
import world.respect.shared.generated.resources.qr_code_badge_description
import world.respect.shared.generated.resources.quick_easy_sign_in
import world.respect.shared.generated.resources.set_password
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
        onAssignQrCodeBadge = viewModel::onAssignQrCodeBadge,
        onSetPassword = viewModel::onSetPassword,
        onLearnMore = viewModel::onLearnMore,
    )
}

@Composable
fun SetUsernameAndPasswordScreen(
    uiState: SetUsernameAndPasswordUiState,
    appUiState: AppUiState,
    onUsernameChanged: (String) -> Unit,
    onAssignQrCodeBadge: () -> Unit,
    onSetPassword: () -> Unit,
    onLearnMore: () -> Unit,
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
                },
        )

        // QR Code Info Box (only for students)
        if (uiState.isStudent) {
            QrCodeInfoBox(
                onLearnMore, onAssignQrCodeBadge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onSetPassword,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            border = ButtonDefaults.outlinedButtonBorder
        ) {
            Text(stringResource(Res.string.set_password))
        }
    }
}

@Composable
fun QrCodeInfoBox(onLearnMore: () -> Unit, onAssignQrCodeBadge: () -> Unit, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Image(
                    painter = respectImagePainter(RespectImage.FINGERPRINT),
                    contentDescription = stringResource(Res.string.qr_code_badge),
                    modifier = Modifier
                        .width(120.dp).height(100.dp)
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.quick_easy_sign_in),
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = stringResource(Res.string.qr_code_badge_description),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.fillMaxWidth()
                    )

                    TextButton(
                        onClick = onLearnMore,
                    ) {
                        Text(stringResource(Res.string.learn_more))
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = onAssignQrCodeBadge,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Text(stringResource(Res.string.assign_qr_code_badge))
            }
        }
    }
}