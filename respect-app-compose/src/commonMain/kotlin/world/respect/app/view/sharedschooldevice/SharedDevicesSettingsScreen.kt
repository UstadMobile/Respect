package world.respect.app.view.sharedschooldevice

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.paging.compose.collectAsLazyPagingItems
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.respectPagingItems
import world.respect.app.components.respectRememberPager
import world.respect.app.components.uiTextStringResource
import world.respect.datalayer.school.PersonDataSource
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.add_device
import world.respect.shared.generated.resources.another_device_add
import world.respect.shared.generated.resources.arrow_down_icon
import world.respect.shared.generated.resources.cancel
import world.respect.shared.generated.resources.check_circle_icon
import world.respect.shared.generated.resources.close_icon
import world.respect.shared.generated.resources.devices
import world.respect.shared.generated.resources.pending_device_requests
import world.respect.shared.generated.resources.phone_android_icon
import world.respect.shared.generated.resources.save
import world.respect.shared.generated.resources.set_pin
import world.respect.shared.generated.resources.share_icon
import world.respect.shared.generated.resources.student_can_self_select_their_class_name
import world.respect.shared.generated.resources.students_must_enter_their_roll_number
import world.respect.shared.generated.resources.tablet_android_last_seen
import world.respect.shared.generated.resources.teacher_admin_unlock_pin
import world.respect.shared.generated.resources.this_device_enable
import world.respect.shared.resources.UiText
import world.respect.shared.viewmodel.sharedschooldevice.SharedDevicesSettingsUiState
import world.respect.shared.viewmodel.sharedschooldevice.SharedDevicesSettingsViewmodel

@Composable
fun SharedDevicesSettingsScreen(
    viewModel: SharedDevicesSettingsViewmodel,
) {
    val uiState by viewModel.uiState.collectAsState()
    println("hgfhjhg ${uiState.pin}")

    SharedDevicesSettingsContent(
        uiState = uiState,
        onToggleSelfSelect = viewModel::toggleSelfSelect,
        onToggleRollNumberLogin = viewModel::toggleRollNumberLogin,
        onShowPinDialog = viewModel::onShowPinDialog,
        onTogglePendingInvites = viewModel::onTogglePendingInvites,
        onApproveDevice = viewModel::onApproveDevice,
        onRejectDevice = viewModel::onRejectDevice,
        onRemoveDevice = viewModel::onRemoveDevice,
        onPinChange = viewModel::onPinChange,
        onSavePin = viewModel::onSavePin,
        onDismissPinDialog = viewModel::onDismissPinDialog,
        onAddAnotherDevice = {
            viewModel.onClickAddAnotherDevice()
            viewModel.onDismissBottomSheet()
        },
        onEnableOnThisDevice = {
            viewModel.onClickEnableOnThisDevice()
            viewModel.onDismissBottomSheet()
        },
        onDismissBottomSheet = viewModel::onDismissBottomSheet,
        onClickAdd = viewModel::onClickAdd,
    )
}

@Composable
private fun SharedDevicesSettingsContent(
    uiState: SharedDevicesSettingsUiState,
    onToggleSelfSelect: (Boolean) -> Unit,
    onToggleRollNumberLogin: (Boolean) -> Unit,
    onShowPinDialog: () -> Unit,
    onTogglePendingInvites: () -> Unit,
    onApproveDevice: (String) -> Unit,
    onRejectDevice: (String) -> Unit,
    onRemoveDevice: (String) -> Unit,
    onPinChange: (String) -> Unit,
    onSavePin: () -> Unit,
    onDismissPinDialog: () -> Unit,
    onAddAnotherDevice: () -> Unit,
    onEnableOnThisDevice: () -> Unit,
    onDismissBottomSheet: () -> Unit,
    onClickAdd: () -> Unit,
) {
    val focusManager = LocalFocusManager.current

    val pager = respectRememberPager(uiState.devices)
    val lazyPagingItems = pager.flow.collectAsLazyPagingItems()

    val pendingPager = respectRememberPager(uiState.pendingDevices)
    val pendingItems = pendingPager.flow.collectAsLazyPagingItems()

    // Handle bottom sheet dismissal
    LaunchedEffect(uiState.showBottomSheetOptions) {
        if (uiState.showBottomSheetOptions) {
            focusManager.clearFocus()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SettingsOptionRow(
                        title = stringResource(Res.string.student_can_self_select_their_class_name),
                        checked = uiState.selfSelectEnabled,
                        onCheckedChange = onToggleSelfSelect
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .clickable { onShowPinDialog() }
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${stringResource(Res.string.teacher_admin_unlock_pin)} ${uiState.pin}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Pending Requests Dropdown Section
            if (pendingItems.itemCount > 0) {
                item("pending_person_header") {
                    ListItem(
                        modifier = Modifier.clickable { onTogglePendingInvites() },
                        headlineContent = {
                            Text(
                                text = stringResource(Res.string.pending_device_requests) +
                                        " (${pendingItems.itemCount})"
                            )
                        },
                        trailingContent = {
                            Icon(
                                imageVector = Icons.Outlined.KeyboardArrowDown,
                                modifier = Modifier
                                    .size(24.dp)
                                    .rotate(if (uiState.isPendingExpanded) 0f else -90f),
                                contentDescription = stringResource(Res.string.arrow_down_icon)
                            )
                        }
                    )
                }
            }

            if (uiState.isPendingExpanded) {
                respectPagingItems(
                    items = pendingItems,
                    key = { item, index -> (item?.guid ?: "") + index.toString() }
                ) { device ->
                    device?.let { device ->
                        ListItem(
                            modifier = Modifier.clickable { },
                            leadingContent = {
                                Icon(
                                    imageVector = Icons.Default.PhoneAndroid,
                                    contentDescription = stringResource(Res.string.phone_android_icon),
                                )
                            },
                            headlineContent = {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Text(
                                        text = device.givenName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )

                                    Text(
                                        text = "${device.metadata} ${
                                            stringResource(
                                                Res.string.tablet_android_last_seen
                                            )
                                        }: ${device.lastModified}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            trailingContent = {
                                Row {
                                    IconButton(
                                        onClick = { onApproveDevice(device.guid) }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = stringResource(Res.string.check_circle_icon),
                                        )
                                    }
                                    IconButton(
                                        onClick = { onRejectDevice(device.guid) }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = stringResource(Res.string.close_icon),
                                        )
                                    }
                                }
                            }
                        )
                    }
                }
            }

            item {
                Text(
                    text = stringResource(Res.string.devices) + " (${lazyPagingItems.itemCount})",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            respectPagingItems(
                items = lazyPagingItems,
                key = { item, index -> item?.guid ?: index.toString() },
                contentType = { PersonDataSource.ENDPOINT_NAME },
            ) { personDetails ->
                personDetails?.let { details ->
                    ListItem(
                        modifier = Modifier.clickable { },
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Default.PhoneAndroid,
                                contentDescription = stringResource(Res.string.phone_android_icon),
                            )
                        },
                        headlineContent = {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(
                                    text = details.givenName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )

                                Text(
                                    text = "${details.metadata} ${
                                        stringResource(
                                            Res.string.tablet_android_last_seen
                                        )
                                    }: ${details.lastModified}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        trailingContent = {
                            IconButton(
                                onClick = { onRemoveDevice(details.guid) }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = stringResource(Res.string.close_icon),
                                )
                            }
                        }
                    )
                }
            }
        }

        // PIN Dialog
        if (uiState.showPinDialog) {
            PinEntryDialog(
                pin = uiState.pin,
                isPinValid = uiState.isPinValid,
                onPinChange = onPinChange,
                onDismiss = onDismissPinDialog,
                onSave = onSavePin,
                errorMessage = uiState.error
            )
        }

        // Bottom Sheet
        if (uiState.showBottomSheetOptions && !uiState.showPinDialog) {
            AddDeviceBottomSheet(
                onDismiss = onDismissBottomSheet,
                onAddAnotherDevice = onAddAnotherDevice,
                onEnableOnThisDevice = onEnableOnThisDevice
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDeviceBottomSheet(
    onDismiss: () -> Unit,
    onAddAnotherDevice: () -> Unit,
    onEnableOnThisDevice: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(Res.string.add_device),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onEnableOnThisDevice)
                    .padding(vertical = 16.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PhoneAndroid,
                    contentDescription = stringResource(Res.string.phone_android_icon),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(Res.string.this_device_enable),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onAddAnotherDevice)
                    .padding(vertical = 16.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = stringResource(Res.string.share_icon),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(Res.string.another_device_add),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinEntryDialog(
    pin: String,
    isPinValid: Boolean,
    onPinChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    errorMessage: UiText? = null,
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(),
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
            ) {
                // Title
                Text(
                    text = stringResource(Res.string.set_pin),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(24.dp))

                BasicTextField(
                    value = pin,
                    onValueChange = { newPin ->
                            onPinChange(newPin)
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.NumberPassword
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = Color(0xFFEEEEEE))
                        .focusRequester(focusRequester)
                        .focusable()
                        .padding(12.dp),
                )
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = uiTextStringResource(errorMessage),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))

                // Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Cancel Text
                    Text(
                        text = stringResource(Res.string.cancel),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .clickable { onDismiss() }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    // Save Text
                    Text(
                        text = stringResource(Res.string.save),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .clickable(
                                onClick = onSave
                            )
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }
            }
        }
    }
}