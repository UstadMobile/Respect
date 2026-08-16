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
import sh.calvin.reorderable.ReorderableListItemScope
import world.respect.app.components.langMapString
import world.respect.lib.opds.model.OpdsPublication
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.move


@Composable
fun ReorderableListItemScope.PlaylistPublicationItemRow(
    publication: OpdsPublication,
    itemIndex: Int,
    groupIndex: Int,
    hasMovableSections: Boolean,
    onClickDelete: () -> Unit,
    onClickMove: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("pub_item_${groupIndex}_$itemIndex"),
    ) {
        IconButton(
            onClick = { },
            modifier = Modifier
                .draggableHandle()
                .testTag("pub_drag_handle_${groupIndex}_$itemIndex")
        ) {
            Icon(
                imageVector = Icons.Filled.DragHandle,
                contentDescription = stringResource(Res.string.move),
            )
        }
        ListItem(
            headlineContent = { Text(text = langMapString(publication.metadata.title)) },
            supportingContent = {
                publication.metadata.description?.let { Text(text = it) }
            },
            modifier = Modifier.weight(1f),
        )
        Box {
            OpdsGroupItemMenuButton(
                groupIndex = groupIndex,
                itemIndex = itemIndex,
                hasMoveOption = hasMovableSections,
                onClickDelete = onClickDelete,
                onClickMove = onClickMove,
            )
        }
    }
}