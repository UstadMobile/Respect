package world.respect.app.view.person.inviteperson

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.alexzhirkevich.qrose.rememberQrCodePainter
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.RespectExposedDropDownMenuField
import world.respect.app.components.defaultItemPadding
import world.respect.datalayer.ext.dataOrNull
import world.respect.datalayer.school.model.PersonRoleEnum
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.approval_not_required_until
import world.respect.shared.generated.resources.approval_required
import world.respect.shared.generated.resources.copy_link
import world.respect.shared.generated.resources.invite_code_label
import world.respect.shared.generated.resources.invite_via_email
import world.respect.shared.generated.resources.invite_via_share
import world.respect.shared.generated.resources.invite_via_sms
import world.respect.shared.generated.resources.qr_code
import world.respect.shared.generated.resources.required
import world.respect.shared.generated.resources.reset_code
import world.respect.shared.generated.resources.role
import world.respect.shared.util.ext.isLoading
import world.respect.shared.util.ext.label
import world.respect.shared.util.rememberFormattedTime
import world.respect.shared.viewmodel.app.appstate.AppUiState
import world.respect.shared.viewmodel.person.inviteperson.InvitePersonUiState
import world.respect.shared.viewmodel.person.inviteperson.InvitePersonViewModel

@Composable
fun InvitePersonScreen(
    viewModel: InvitePersonViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val appUiState by viewModel.appUiState.collectAsState()

    InvitePersonScreen(
        uiState = uiState,
        appUiState = appUiState,
        onCopyLink = viewModel::copyInviteLinkToClipboard,
        onInviteViaSms = viewModel::onSendLinkViaSms,
        onInviteViaEmail = viewModel::onSendLinkViaEmail,
        onInviteViaShare = viewModel::onShareLink,
        onApprovalRequiredChanged = viewModel::onApprovalEnabledChanged,
        onRoleChange = viewModel::onRoleChange,
        onClickResetCode = viewModel::onClickResetCode,
    )
}

@Composable
fun InvitePersonScreen(
    uiState: InvitePersonUiState,
    appUiState: AppUiState,
    onCopyLink: () -> Unit,
    onInviteViaSms: () -> Unit,
    onInviteViaEmail: () -> Unit,
    onInviteViaShare: () -> Unit,
    onApprovalRequiredChanged: (Boolean) -> Unit,
    onRoleChange: (PersonRoleEnum) -> Unit,
    onClickResetCode: () -> Unit,
) {
    val invite = uiState.invite.dataOrNull()
    val fieldsEnabled = !appUiState.isLoading && invite != null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {

        val selectedRole = uiState.selectedRole ?: uiState.roleOptions.firstOrNull()
            ?: PersonRoleEnum.STUDENT

        RespectExposedDropDownMenuField(
            value = selectedRole,
            modifier = Modifier.defaultItemPadding().fillMaxWidth().testTag("role"),
            label = {
                Text(stringResource(Res.string.role))
            },
            onOptionSelected = { newRole ->
                onRoleChange(newRole)
            },
            options = uiState.roleOptions,
            itemText = { stringResource(it.label) },
            enabled = fieldsEnabled,
        )

        uiState.inviteUrl?.also { link ->
            val linkStr = link.toString()
            Image(
                painter = rememberQrCodePainter(link.toString()),
                contentDescription = stringResource(Res.string.qr_code),
                modifier = Modifier
                    .size(240.dp)
                    .defaultItemPadding()
                    .align(Alignment.CenterHorizontally),
            )

            Text(
                text = linkStr,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .testTag("invite_url")
                    .clickable(enabled = fieldsEnabled) { onInviteViaShare() }
                    .defaultItemPadding()
                    .align(Alignment.CenterHorizontally)
            )

        }

        invite?.also {
            Text(
                text = "${stringResource(Res.string.invite_code_label)}: ${it.code}",
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .testTag("invite_code")
                    .align(Alignment.CenterHorizontally)
                    .defaultItemPadding()
            )
        }

        HorizontalDivider()

        val approvalRequired = uiState.approvalRequired
        val approvalRequiredAfter = invite?.approvalRequiredAfter

        ListItem(
            modifier = Modifier.clickable(enabled = fieldsEnabled) {
                onApprovalRequiredChanged(!approvalRequired)
            },
            headlineContent = { Text(stringResource(Res.string.approval_required)) },
            trailingContent = {
                Switch(
                    checked = approvalRequired,
                    enabled = fieldsEnabled,
                    onCheckedChange = { onApprovalRequiredChanged(it) }
                )
            },
            leadingContent = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
            supportingContent = if(!approvalRequired && approvalRequiredAfter != null) {
                {
                    val approvalOffUntil = rememberFormattedTime(approvalRequiredAfter)
                    Text(stringResource(Res.string.approval_not_required_until) + " " + approvalOffUntil)
                }
            }else {
                null
            }
        )

        HorizontalDivider()

        ListItem(
            modifier = Modifier.clickable(enabled = fieldsEnabled) { onCopyLink() },
            leadingContent = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
            headlineContent = { Text(stringResource(Res.string.copy_link)) }
        )

        ListItem(
            modifier = Modifier.clickable(enabled = fieldsEnabled) { onInviteViaSms() },
            leadingContent = { Icon(Icons.Default.Sms, contentDescription = null) },
            headlineContent = { Text(stringResource(Res.string.invite_via_sms)) }
        )

        ListItem(
            modifier = Modifier.clickable(enabled = fieldsEnabled) { onInviteViaEmail() },
            leadingContent = { Icon(Icons.Default.Email, contentDescription = null) },
            headlineContent = { Text(stringResource(Res.string.invite_via_email)) }
        )

        ListItem(
            modifier = Modifier.clickable(enabled = fieldsEnabled) { onInviteViaShare() },
            leadingContent = { Icon(Icons.Default.Share, contentDescription = null) },
            headlineContent = { Text(stringResource(Res.string.invite_via_share)) }
        )

        ListItem(
            modifier = Modifier.clickable(enabled = fieldsEnabled) { onClickResetCode() },
            leadingContent = { Icon(Icons.Default.Refresh, contentDescription = null) },
            headlineContent = { Text(stringResource(Res.string.reset_code)) }
        )
    }
}
