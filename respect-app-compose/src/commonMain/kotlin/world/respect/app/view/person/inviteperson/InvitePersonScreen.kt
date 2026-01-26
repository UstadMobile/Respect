package world.respect.app.view.person.inviteperson

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import kotlinx.coroutines.Dispatchers
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.RespectExposedDropDownMenuField
import world.respect.app.components.defaultItemPadding
import world.respect.datalayer.ext.dataOrNull
import world.respect.datalayer.school.model.PersonRoleEnum
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.approval_not_required_until
import world.respect.shared.generated.resources.approval_required
import world.respect.shared.generated.resources.class_name
import world.respect.shared.generated.resources.copy_link
import world.respect.shared.generated.resources.school_name
import world.respect.shared.generated.resources.invite_code_label
import world.respect.shared.generated.resources.invite_via_email
import world.respect.shared.generated.resources.invite_via_share
import world.respect.shared.generated.resources.invite_via_sms
import world.respect.shared.generated.resources.qr_code
import world.respect.shared.generated.resources.required
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
        onClickQrCode = viewModel::onClickQrCode,
        onApprovalRequiredChanged = viewModel::onApprovalEnabledChanged,
        onRoleChange = viewModel::onRoleChange,
        onClickGetCode = viewModel::onClickGetCode
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
    onClickQrCode: () -> Unit,
    onClickGetCode: () -> Unit,
    onApprovalRequiredChanged: (Boolean) -> Unit,
    onRoleChange: (PersonRoleEnum) -> Unit
) {
    val invite = uiState.invite.dataOrNull()
    val isLoading = appUiState.isLoading

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
                Text(stringResource(Res.string.role) + "*")
            },
            onOptionSelected = { newRole ->
                onRoleChange(newRole)
            },
            options = uiState.roleOptions,
            itemText = { stringResource(it.label) },
            enabled = !isLoading,
            supportingText = {
                Text(stringResource(Res.string.required))
            }
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .defaultItemPadding()

        ) {
            val res = if (uiState.className != null) {
                Res.string.class_name
            } else {
                Res.string.school_name
            }
            ListItem(
                headlineContent = { Text(stringResource(res)) },
                supportingContent = { Text(text = uiState.className ?: uiState.schoolName ?: "") }
            )
        }

        HorizontalDivider()

        val approvalRequired = uiState.approvalRequired
        val approvalRequiredAfter = invite?.approvalRequiredAfter

        ListItem(
            modifier = Modifier.clickable(enabled = !isLoading) {
                onApprovalRequiredChanged(!approvalRequired)
            },
            headlineContent = { Text(stringResource(Res.string.approval_required)) },
            trailingContent = {
                Switch(
                    checked = approvalRequired,
                    enabled = !isLoading,
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

        Text(uiState.inviteCode ?: "")

        ListItem(
            modifier = Modifier.clickable(enabled = !isLoading) { onCopyLink() },
            leadingContent = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
            headlineContent = { Text(stringResource(Res.string.copy_link)) }
        )

        ListItem(
            modifier = Modifier.clickable(enabled = !isLoading) { onInviteViaSms() },
            leadingContent = { Icon(Icons.Default.Sms, contentDescription = null) },
            headlineContent = { Text(stringResource(Res.string.invite_via_sms)) }
        )

        ListItem(
            modifier = Modifier.clickable(enabled = !isLoading) { onInviteViaEmail() },
            leadingContent = { Icon(Icons.Default.Email, contentDescription = null) },
            headlineContent = { Text(stringResource(Res.string.invite_via_email)) }
        )

        ListItem(
            modifier = Modifier.clickable(enabled = !isLoading) { onInviteViaShare() },
            leadingContent = { Icon(Icons.Default.Share, contentDescription = null) },
            headlineContent = { Text(stringResource(Res.string.invite_via_share)) }
        )
        ListItem(
            modifier = Modifier.clickable(enabled = !isLoading) { onClickQrCode() },
            leadingContent = { Icon(Icons.Default.QrCode, contentDescription = null) },
            headlineContent = { Text(stringResource(Res.string.qr_code)) }
        )

        ListItem(
            modifier = Modifier.clickable(enabled = !isLoading) { onClickGetCode() },
            leadingContent = { Icon(Icons.Default.Code, contentDescription = null) },
            headlineContent = { Text(stringResource(Res.string.invite_code_label)) }
        )
    }
}
