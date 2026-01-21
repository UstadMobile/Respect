package world.respect.app.view.manageuser.sharefeedback

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import world.respect.shared.viewmodel.manageuser.sharefeedback.ShareFeedbackUiState
import world.respect.shared.viewmodel.manageuser.sharefeedback.ShareFeedbackViewModel
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Whatsapp
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.defaultItemPadding
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.email_support
import world.respect.shared.generated.resources.public_forum
import world.respect.shared.generated.resources.quick_contact
import world.respect.shared.generated.resources.whatsapp_support

@Composable
fun ShareFeedbackScreen(
    viewModel: ShareFeedbackViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()
    ShareFeedbackScreen(
        uiState = uiState,
        onClickWhatsApp = viewModel::onClickWhatsApp,
        onClickEmail = viewModel::onClickEmail,
        onClickPublicForum = viewModel::onClickPublicForum
    )
}

@Composable
fun ShareFeedbackScreen(
    uiState: ShareFeedbackUiState,
    onClickWhatsApp: () -> Unit,
    onClickEmail: () -> Unit,
    onClickPublicForum: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth()
    ) {

        item {
            Text(
                modifier = Modifier.defaultItemPadding(),
                text = stringResource(Res.string.quick_contact)
            )
        }

        item {
            FeedbackItem(
                title = stringResource(Res.string.whatsapp_support),
                leadingIcon = Icons.Outlined.Whatsapp,
                contentDescription = "Open Whatsapp Support",
                onClick = onClickWhatsApp
            )
        }

        item {
            FeedbackItem(
                title = stringResource(Res.string.email_support),
                leadingIcon = Icons.Outlined.Email,
                contentDescription = "Open Email Support",
                onClick = onClickEmail
            )
        }

        item {
            FeedbackItem(
                title = stringResource(Res.string.public_forum),
                leadingIcon = Icons.Outlined.People,
                contentDescription = "Open Public Forum",
                onClick = onClickPublicForum,
            )
        }
    }
}

@Composable
private fun FeedbackItem(
    title: String,
    leadingIcon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = {
            Text(text = title)
        },
        leadingContent = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = title
            )
        },
        trailingContent = {
            Icon(
                imageVector = Icons.Outlined.OpenInNew,
                contentDescription = contentDescription
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 8.dp)
    )
}
