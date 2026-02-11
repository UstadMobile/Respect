package world.respect.app.view.sharedschooldevice

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.defaultItemPadding
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.device_name
import world.respect.shared.generated.resources.enable_button
import world.respect.shared.generated.resources.image_shared_device
import world.respect.shared.generated.resources.shared_device
import world.respect.shared.generated.resources.shared_device_description_1
import world.respect.shared.generated.resources.shared_device_description_2
import world.respect.shared.generated.resources.shared_device_description_3
import world.respect.shared.generated.resources.undraw_sync_pe2t_1
import world.respect.shared.viewmodel.sharedschooldevice.SharedSchoolDeviceEnableUiState
import world.respect.shared.viewmodel.sharedschooldevice.SharedSchoolDeviceEnableViewmodel

@Composable
fun SharedSchoolDeviceEnableScreen(
    viewModel: SharedSchoolDeviceEnableViewmodel,
) {
    val uiState by viewModel.uiState.collectAsState()

    SharedSchoolDeviceEnableScreenContent(
        uiState = uiState,
        onDeviceNameChange = viewModel::updateDeviceName,
        onEnableSharedDeviceMode = viewModel::enableSharedDeviceMode,
    )
}

@Composable
fun SharedSchoolDeviceEnableScreenContent(
    uiState: SharedSchoolDeviceEnableUiState = SharedSchoolDeviceEnableUiState(),
    onDeviceNameChange: (String) -> Unit = {},
    onEnableSharedDeviceMode: () -> Unit = {},
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .defaultItemPadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = stringResource(Res.string.device_name),
                style = MaterialTheme.typography.bodyLarge
            )
        }
        item {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("device_name_input"),
                value = uiState.deviceName,
                label = { Text("${stringResource(Res.string.device_name)} *") },
                onValueChange = onDeviceNameChange,
                singleLine = true,
                isError = !uiState.isDeviceNameValid && uiState.deviceName.isNotEmpty(),
                supportingText = {
                    if (!uiState.isDeviceNameValid && uiState.deviceName.isNotEmpty()) {
                        Text("Device name is required")
                    }
                }
            )
        }
        item {
            SharedSchoolDeviceInfoBox(
                onClickEnableSharedSchoolDeviceMode = onEnableSharedDeviceMode
            )
        }
    }
}

@Composable
private fun SharedSchoolDeviceInfoBox(
    onClickEnableSharedSchoolDeviceMode: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Image(
                    painter = painterResource(Res.drawable.undraw_sync_pe2t_1),
                    contentDescription = stringResource(Res.string.image_shared_device),
                    modifier = Modifier
                        .width(120.dp)
                        .height(100.dp)
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.shared_device),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = " * ${stringResource(Res.string.shared_device_description_1)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = " * ${stringResource(Res.string.shared_device_description_2)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = " * ${stringResource(Res.string.shared_device_description_3)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Button(
                onClick = onClickEnableSharedSchoolDeviceMode,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("enable_button"),
                enabled = true,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
            ) {
                Text(stringResource(Res.string.enable_button))
            }
        }
    }
}
