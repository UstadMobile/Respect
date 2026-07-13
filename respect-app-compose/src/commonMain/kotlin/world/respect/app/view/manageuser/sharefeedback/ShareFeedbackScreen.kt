package world.respect.app.view.manageuser.sharefeedback

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import world.respect.shared.viewmodel.manageuser.sharefeedback.ShareFeedbackViewModel
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Whatsapp
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.defaultScreenPadding
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.email_respect
import world.respect.shared.generated.resources.public_forum
import world.respect.shared.generated.resources.whatsapp_respect

@Composable
fun ShareFeedbackScreen(
    viewModel: ShareFeedbackViewModel
) {
    ShareFeedbackScreen(
        onClickWhatsApp = viewModel::onClickWhatsApp,
        onClickEmail = viewModel::onClickEmail,
        onClickPublicForum = viewModel::onClickPublicForum
    )
}

@Composable
fun ShareFeedbackScreen(
    onClickWhatsApp: () -> Unit,
    onClickEmail: () -> Unit,
    onClickPublicForum: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .defaultScreenPadding()
    ) {


        item {
            QuickContactItem(
                title = stringResource(Res.string.whatsapp_respect),
                leadingIcon = Icons.Outlined.Whatsapp,
                onClick = onClickWhatsApp
            )
        }

        item {
            QuickContactItem(
                title = stringResource(Res.string.email_respect),
                leadingIcon = Icons.Outlined.Email,
                onClick = onClickEmail
            )
        }

        item {
            QuickContactItem(
                title = stringResource(Res.string.public_forum),
                leadingIcon = Icons.Outlined.People,
                onClick = onClickPublicForum,
            )
        }
    }
}

@Composable
private fun QuickContactItem(
    title: String,
    leadingIcon: ImageVector,
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .clickable { onClick() }
    )
}

