package world.respect.app.view.manageuser.accountlist

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.RespectPersonAvatar
import world.respect.datalayer.db.school.ext.fullName
import world.respect.datalayer.school.model.PersonWithEnrollment
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.dismiss_invite
import world.respect.shared.generated.resources.pending_approval

@Composable
fun PendingPersonEnrollmentItem(
    personWithEnrollment: PersonWithEnrollment?,
) {
    ListItem(
        modifier = Modifier.fillMaxWidth(),
        leadingContent = {

            RespectPersonAvatar(
                name = personWithEnrollment?.person?.fullName() + " : "
            )
        },
        headlineContent = {
            Text(
                text = personWithEnrollment?.person?.fullName() + " : "
                        +personWithEnrollment?.clazz?.title +
                        "(${personWithEnrollment?.person?.roles?.firstOrNull()?.roleEnum?.name})"
            )
        },
        supportingContent = {
            val pendingApproval = personWithEnrollment?.enrollment?.endDate?.toEpochDays() ?: ""
            Text(
                text = "$pendingApproval  ${stringResource(Res.string.pending_approval)}"
            )

        },
        trailingContent = {


            IconButton(
                onClick = {
                },
                enabled = personWithEnrollment != null
            ) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    imageVector = Icons.Outlined.Cancel,
                    contentDescription = stringResource(resource = Res.string.dismiss_invite)
                )
            }
        }
    )
}