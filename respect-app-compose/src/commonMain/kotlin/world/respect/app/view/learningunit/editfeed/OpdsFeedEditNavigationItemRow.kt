package world.respect.app.view.learningunit.editfeed

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import org.jetbrains.compose.resources.stringResource
import world.respect.lib.opds.model.ReadiumLink
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.move


@Composable
fun OpdsFeedEditNavigationItemRow(
    navItem: ReadiumLink,
    itemIndex: Int,
    groupIndex: Int,
    hasMoveOption: Boolean,
    dragHandleModifier: Modifier,
    onClickDelete: () -> Unit,
    onClickMove: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("nav_item_${groupIndex}_$itemIndex"),
    ) {
        IconButton(
            onClick = {},
            modifier = dragHandleModifier
        ) {
            Icon(
                imageVector = Icons.Filled.DragHandle,
                contentDescription = stringResource(Res.string.move),
            )
        }
        ListItem(
            headlineContent = {
                Text(
                    text = navItem.title?.takeIf { it.isNotBlank() } ?: navItem.href,
                )
            },
            modifier = Modifier.weight(1f),
        )
        Box {
            OpdsGroupItemMenuButton(
                sectionIndex = groupIndex,
                itemIndex = itemIndex,
                hasMoveOption = hasMoveOption,
                onClickDelete = onClickDelete,
                onClickMove = onClickMove,
            )
        }
    }
}