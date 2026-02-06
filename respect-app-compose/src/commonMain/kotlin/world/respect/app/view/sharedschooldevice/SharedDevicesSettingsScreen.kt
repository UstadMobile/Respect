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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    val focusManager = LocalFocusManager.current

    val pager = respectRememberPager(uiState.devices)
    val lazyPagingItems = pager.flow.collectAsLazyPagingItems()

    var showPendingRequests by remember { mutableStateOf(false) }
    val pendingRequests = listOf("Device 4")

    // Handle bottom sheet dismissal
    LaunchedEffect(uiState.showBottomSheetOptions) {
        if (uiState.showBottomSheetOptions) {
            focusManager.clearFocus() // Clear focus to prevent keyboard issues
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
                        onCheckedChange = { viewModel.toggleSelfSelect(it) }
                    )

                    SettingsOptionRow(
                        title = stringResource(Res.string.students_must_enter_their_roll_number),
                        checked = uiState.rollNumberLoginEnabled,
                        onCheckedChange = { viewModel.toggleRollNumberLogin(it) }
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .clickable { viewModel.onShowPinDialog() }
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Teacher/admin unlock PIN \n" +
                                "5464",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Pending Requests Dropdown Section
            item {
                Column {
                    Row(
                        modifier = Modifier
                            .clickable { showPendingRequests = !showPendingRequests }
                            .padding(4.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Pending device request to join (${pendingRequests.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Icon(
                            imageVector = if (showPendingRequests)
                                Icons.Default.ArrowDropUp
                            else
                                Icons.Default.ArrowDropDown,
                            contentDescription = if (showPendingRequests) "Hide" else "Show"
                        )
                    }

                    if (showPendingRequests) {
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 16.dp)
                        ) {
                            pendingRequests.forEach { userName ->
                                PendingRequestItem(userName)
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = stringResource(Res.string.devices) + "(${lazyPagingItems.itemCount})",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            respectPagingItems(
                items = lazyPagingItems,
                key = { item, index -> item?.guid ?: index.toString() },
                contentType = { PersonDataSource.ENDPOINT_NAME },
            ) { person ->
                ListItem(
                    modifier = Modifier.clickable { },
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
                                text = person?.givenName ?: "Device name",
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

        // PIN Dialog - should be on top of everything
        if (uiState.showPinDialog) {
            PinEntryDialog(
                pin = uiState.pin,
                onPinChange = viewModel::onPinChange,
                onDismiss = viewModel::onDismissPinDialog,
                onSave = viewModel::onSavePin
            )
        }

        // Bottom Sheet - should be separate from dialog
        if (uiState.showBottomSheetOptions && !uiState.showPinDialog) {
            AddDeviceBottomSheet(
                onDismiss = {
                    // Create a function in ViewModel to dismiss bottom sheet
                    viewModel.onDismissBottomSheet()
                },
                onAddAnotherDevice = {
                    viewModel.onClickAddAnotherDevice()
                    // Dismiss after selection
                    viewModel.onDismissBottomSheet()
                },
                onEnableOnThisDevice = {
                    viewModel.onClickEnableOnThisDevice()
                    // Dismiss after selection
                    viewModel.onDismissBottomSheet()
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
                text = "Add Device",
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
                    contentDescription = "PhoneAndroid",
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "This device Enable shared school device on mode on this device",
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
                    contentDescription = "Share",
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Another device Add using QR code, link, or invite code",
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun PendingRequestItem(userName: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.PhoneAndroid,
                contentDescription = null,
            )
            Text(
                text = userName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
        }

        Row {
            IconButton(
                onClick = { /* Handle approve */ }
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Approve",
                )
            }
            IconButton(
                onClick = { /* Handle reject */ }
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Reject",
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinEntryDialog(
    pin: String,
    onPinChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        // Auto-focus and open keyboard when dialog appears
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
                    text = "Set PIN",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(24.dp))

                BasicTextField(
                    value = pin,
                    onValueChange = { newPin ->
                        if (newPin.length <= 4 && newPin.all { it.isDigit() }) {
                            onPinChange(newPin)
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = Color(0xFFEEEEEE))
                        .focusRequester(focusRequester)
                        .focusable()
                        .padding(12.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Cancel Text
                    Text(
                        text = "Cancel",
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
                        text = "Save",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .clickable(
                                enabled = pin.length == 4,
                                onClick = onSave
                            )
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }
            }
        }
    }
}