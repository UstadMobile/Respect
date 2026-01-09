package world.respect.app.view.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.cancel
import world.respect.shared.generated.resources.device_name
import world.respect.shared.generated.resources.devices
import world.respect.shared.generated.resources.empty
import world.respect.shared.generated.resources.enable_shared_device_mode
import world.respect.shared.generated.resources.ok
import world.respect.shared.generated.resources.student_can_self_select_their_class_name
import world.respect.shared.generated.resources.students_must_enter_their_roll_number
import world.respect.shared.viewmodel.settings.SharedDevicesSettingsViewmodel

@Composable
fun SharedDevicesSettingsScreen(
    viewModel: SharedDevicesSettingsViewmodel,
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SharedSchoolDeviceInfoBox(
            onClickEnableSharedSchoolDeviceMode = { viewModel.onClickEnableSharedSchoolDeviceMode() },
            modifier = Modifier.fillMaxWidth()
        )

        // Student login options with toggles
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Option 1: Self-select class and name
            SettingsOptionRow(
                title = stringResource(Res.string.student_can_self_select_their_class_name),
                checked = uiState.selfSelectEnabled,
                onCheckedChange = { viewModel.toggleSelfSelect(it) }
            )

            // Option 2: Roll number login
            SettingsOptionRow(
                title = stringResource(Res.string.students_must_enter_their_roll_number),
                checked = uiState.rollNumberLoginEnabled,
                onCheckedChange = { viewModel.toggleRollNumberLogin(it) }
            )
        }

        // Devices section
        Text(
            text = stringResource(Res.string.devices) + "(${uiState.school.size})",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth()
        )

        // Devices list
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Repeat for each device (6 devices shown in screenshot)
            uiState.school.forEach {
                DeviceItem(
                    deviceId = "12345",
                    deviceType = "Tablet (Android 14)",
                    lastSeen = "9/12/25 13:42",
                    isSelected = false,
                    onSelectionChanged = { /* Handle selection */ }
                )
            }
        }
    }

    // Show the enable shared device mode dialog
    if (uiState.showEnableDialog) {
        EnableSharedDeviceDialog(
            deviceName = uiState.deviceName,
            onDismiss = { viewModel.onDismissEnableDialog() },
            onConfirm = {
                viewModel.onConfirmEnableDialog(
                    localDeviceName = it
                )
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnableSharedDeviceDialog(
    deviceName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var localDeviceName by remember { mutableStateOf(deviceName) }
    val focusManager = LocalFocusManager.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(Res.string.enable_shared_device_mode),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                OutlinedTextField(
                    value = localDeviceName,
                    onValueChange = { localDeviceName = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                        }
                    ),
                    label = {
                        Text(
                            text = stringResource(Res.string.device_name),
                        )
                    }
                )
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onDismiss
                ) {
                    Text(
                        text = stringResource(Res.string.cancel),
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        onConfirm(localDeviceName)
                    }
                ) {
                    Text(
                        text = stringResource(Res.string.ok),
                    )
                }
            }
        }
    )
}

@Composable
private fun SettingsOptionRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun DeviceItem(
    deviceId: String,
    deviceType: String,
    lastSeen: String,
    isSelected: Boolean,
    onSelectionChanged: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Checkbox for device selection
            Icon(
                imageVector = Icons.Default.Phone,
                contentDescription = null,
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = deviceId,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = "$deviceType. Last seen: $lastSeen",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SharedSchoolDeviceInfoBox(
    onClickEnableSharedSchoolDeviceMode: () -> Unit,
    modifier: Modifier,
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
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Image(
                    painter = painterResource(Res.drawable.empty),
                    contentDescription = "",
                    modifier = Modifier
                        .width(120.dp).height(100.dp)
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Shared school devices make it easy for any student to sign-in on any device and for administrators to manage devices.",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = onClickEnableSharedSchoolDeviceMode,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                border = ButtonDefaults.outlinedButtonBorder
            ) {
                Text("Enable shared device mode on this device")
            }
        }
    }
}