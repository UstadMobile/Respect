package world.respect.app.view.playlists.mapping.share

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import qrgenerator.qrkitpainter.rememberQrKitPainter
import world.respect.app.components.defaultItemPadding
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.anyone_with_the_link
import world.respect.shared.generated.resources.copy_link
import world.respect.shared.generated.resources.send_link_via_email
import world.respect.shared.generated.resources.send_link_via_sms
import world.respect.shared.generated.resources.share_link
import world.respect.shared.generated.resources.teacher_admin_in_my_school
import world.respect.shared.generated.resources.who_can_edit
import world.respect.shared.generated.resources.who_can_view
import world.respect.shared.viewmodel.playlists.mapping.share.PlaylistShareUiState
import world.respect.shared.viewmodel.playlists.mapping.share.PlaylistShareViewModel


@Composable
fun PlaylistShareScreen(
    viewModel: PlaylistShareViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    PlaylistShareScreen(
        uiState = uiState,
        onClickShareLink = viewModel::onClickShareLink,
        onClickCopyLink = viewModel::onClickCopyLink,
        onClickSendViaSms = viewModel::onClickSendViaSms,
        onClickSendViaEmail = viewModel::onClickSendViaEmail,
        onViewPermissionChanged = viewModel::onViewPermissionChanged,
        onEditPermissionChanged = viewModel::onEditPermissionChanged,
    )
}

@Composable
fun PlaylistShareScreen(
    uiState: PlaylistShareUiState,
    onClickShareLink: () -> Unit = {},
    onClickCopyLink: () -> Unit = {},
    onClickSendViaSms: () -> Unit = {},
    onClickSendViaEmail: () -> Unit = {},
    onViewPermissionChanged: (String) -> Unit = {},
    onEditPermissionChanged: (String) -> Unit = {},
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        item("qr_code") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val shareUrlStr = uiState.shareUrl?.toString().orEmpty()

                if (shareUrlStr.isNotEmpty()) {
                    val qrCodePainter = rememberQrKitPainter(data = shareUrlStr)

                    Image(
                        painter = qrCodePainter,
                        contentDescription = "QR Code for sharing playlist",
                        modifier = Modifier.size(200.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = shareUrlStr,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item("permissions") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultItemPadding()
            ) {
                PermissionDropdown(
                    label = stringResource(Res.string.who_can_view),
                    selectedValue = uiState.viewPermission,
                    options = listOf(
                        stringResource(Res.string.anyone_with_the_link),
                        stringResource(Res.string.teacher_admin_in_my_school)
                    ),
                    onValueChanged = onViewPermissionChanged
                )

                Spacer(modifier = Modifier.height(16.dp))

                PermissionDropdown(
                    label = stringResource(Res.string.who_can_edit),
                    selectedValue = uiState.editPermission,
                    options = listOf(
                        stringResource(Res.string.teacher_admin_in_my_school)
                    ),
                    onValueChanged = onEditPermissionChanged
                )
            }
        }

        item("share_link") {
            ListItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClickShareLink() },
                leadingContent = {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = null
                    )
                },
                headlineContent = {
                    Text(stringResource(Res.string.share_link))
                }
            )
        }

        item("copy_link") {
            ListItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClickCopyLink() },
                leadingContent = {
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = null
                    )
                },
                headlineContent = {
                    Text(stringResource(Res.string.copy_link))
                }
            )
        }

        item("send_sms") {
            ListItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClickSendViaSms() },
                leadingContent = {
                    Icon(
                        Icons.Default.Sms,
                        contentDescription = null
                    )
                },
                headlineContent = {
                    Text(stringResource(Res.string.send_link_via_sms))
                }
            )
        }

        item("send_email") {
            ListItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClickSendViaEmail() },
                leadingContent = {
                    Icon(
                        Icons.Default.Email,
                        contentDescription = null
                    )
                },
                headlineContent = {
                    Text(stringResource(Res.string.send_link_via_email))
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PermissionDropdown(
    label: String,
    selectedValue: String,
    options: List<String>,
    onValueChanged: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selectedValue,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onValueChanged(option)
                        expanded = false
                    }
                )
            }
        }
    }
}