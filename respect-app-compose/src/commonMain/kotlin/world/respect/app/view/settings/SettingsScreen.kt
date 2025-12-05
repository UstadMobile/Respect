package world.respect.app.view.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.DevicesFold
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.loading
import world.respect.shared.generated.resources.mappings
import world.respect.shared.viewmodel.settings.SettingsUiState
import world.respect.shared.viewmodel.settings.SettingsViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel
) {
    val uiState: SettingsUiState by viewModel.uiState.collectAsState(SettingsUiState())

    SettingsScreen(
        uiState = uiState,
        onNavigateToMapping = viewModel::onNavigateToMapping,
        onToggleSharedDevice = viewModel::onToggleSharedDevice,
        onDismissSharedDeviceDialog = viewModel::onClickOkay
    )
}

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onNavigateToMapping: () -> Unit = {},
    onToggleSharedDevice: (Boolean) -> Unit = {},
    onDismissSharedDeviceDialog: () -> Unit = {},
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp),
    ) {
        item {
            SharedSchoolDevice(
                isSharedDevice = uiState.isSharedDevice,
                onToggleSharedDevice = onToggleSharedDevice
            )
        }
        item {
            SettingsListItem(
                icon = Icons.Filled.Map,
                title = stringResource(Res.string.mappings),
                onClick = onNavigateToMapping,
                testTag = "mapping_setting_item"
            )
        }
    }
    // Show the dialog when needed
    if (uiState.showSharedDeviceDialog) {
        AlertDialog(
            containerColor = MaterialTheme.colorScheme.background,
            onDismissRequest = onDismissSharedDeviceDialog,
            confirmButton = {
                Button(
                    onClick = onDismissSharedDeviceDialog,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("shared_device_ok_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Text(
                        text = "Okay",
                    )
                }
            },
            text = {
                Text(
                    text = "This device will change to shared device once this setting is on",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            },
            modifier = Modifier.testTag("shared_device_dialog")
        )
    }
}

@Composable
fun SharedSchoolDevice(
    isSharedDevice: Boolean,
    onToggleSharedDevice: (Boolean) -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleSharedDevice(!isSharedDevice) },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.PhoneAndroid,
                contentDescription = null,
                modifier = Modifier.padding(end = 16.dp),
            )
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Shared school device",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Mark to make a device a shared school mode",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }


            Switch(
                checked = isSharedDevice,
                onCheckedChange = onToggleSharedDevice,
                modifier = Modifier
                    .testTag("shared_device_toggle")
                    .scale(0.6f),

                )
        }

        if (isSharedDevice) {
            Text(
                text = "This device will change to shared device once this setting is on",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SettingsListItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit,
    testTag: String
) {
    ListItem(
        headlineContent = {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
        },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = stringResource(Res.string.loading),
                tint = MaterialTheme.colorScheme.onSurface
            )
        },
        modifier = Modifier
            .testTag(testTag)
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        tonalElevation = 0.dp
    )
}
