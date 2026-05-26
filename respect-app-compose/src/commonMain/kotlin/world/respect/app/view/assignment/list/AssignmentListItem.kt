package world.respect.app.view.assignment.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import world.respect.lib.xapi.model.AssignmentSummary
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.completed_status
import world.respect.shared.generated.resources.menu
import world.respect.shared.generated.resources.task

@Composable
fun AssignmentListItem(
    summary: AssignmentSummary,
    onClick: (AssignmentSummary) -> Unit,
) {
    ListItem(
        modifier = Modifier.clickable {
            onClick(summary)
        },
        headlineContent = {
            Text(
                text = summary.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                summary.assignedActor.name?.also {  assignedToName ->
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = stringResource(Res.string.menu),
                        modifier = Modifier.size(16.dp),
                    )

                    Spacer(Modifier.width(8.dp))

                    Text(assignedToName)

                    Spacer(Modifier.width(16.dp))
                }

                Icon(
                    imageVector = Icons.Default.TaskAlt,
                    contentDescription = stringResource(Res.string.task),
                    modifier = Modifier.size(16.dp),
                )

                Spacer(Modifier.width(8.dp))

                Text("${summary.completedCount}/${summary.totalCount} ${stringResource(Res.string.completed_status)}")
            }
        },
        trailingContent = summary.averageScore?.let {
            {
                Text(it.toString())
            }
        }
    )
}