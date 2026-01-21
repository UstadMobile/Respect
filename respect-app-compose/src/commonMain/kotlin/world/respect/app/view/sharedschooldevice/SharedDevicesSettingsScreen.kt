package world.respect.app.view.sharedschooldevice

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.respectPagingItems
import world.respect.app.components.respectRememberPager
import world.respect.datalayer.school.PersonDataSource
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.devices
import world.respect.shared.generated.resources.student_can_self_select_their_class_name
import world.respect.shared.generated.resources.students_must_enter_their_roll_number
import world.respect.shared.viewmodel.sharedschooldevice.SharedDevicesSettingsViewmodel

@Composable
fun SharedDevicesSettingsScreen(
    viewModel: SharedDevicesSettingsViewmodel,
) {
    val uiState by viewModel.uiState.collectAsState()

    val pager = respectRememberPager(uiState.devices)

    val lazyPagingItems = pager.flow.collectAsLazyPagingItems()
    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
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
        }
        item {
            // Devices section
            Text(
                text = stringResource(Res.string.devices) + "(${lazyPagingItems.itemCount})",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Devices list
        respectPagingItems(
            items = lazyPagingItems,
            key = { item, index -> item?.guid ?: index.toString() },
            contentType = { PersonDataSource.ENDPOINT_NAME },
        ) { person ->
            ListItem(
                modifier = Modifier.clickable {
                },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.PhoneAndroid,
                        contentDescription = null,
                    )
                },
                headlineContent = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = "Device Name",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )

                        Text(
                            text = "Tablet (Android 14), last seen: 9/12/25, 14:12",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                trailingContent = {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                    )
                }
            )
        }
    }
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