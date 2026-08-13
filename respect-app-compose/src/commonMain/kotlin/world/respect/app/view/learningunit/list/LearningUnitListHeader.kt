package world.respect.app.view.learningunit.list

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Task
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.RespectQuickActionButton
import world.respect.app.components.defaultItemPadding
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.assign
import world.respect.shared.generated.resources.copy_playlist
import world.respect.shared.generated.resources.delete
import world.respect.shared.generated.resources.share
import world.respect.shared.viewmodel.learningunit.list.LearningUnitListUiState


@Composable
fun FeedHeader(
    uiState: LearningUnitListUiState,
    onClickShare: () -> Unit,
    onClickCopy: () -> Unit,
    onClickDelete: () -> Unit,
    onClickAssign: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        ListItem(
            leadingContent = {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(48.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    /* TODO: Fix this - its using nonsensical logic
                    RespectAsyncImage(
                        uri = uiState.group
                            .flatMap { it.publications ?: emptyList() }
                            .firstOrNull()
                            ?.images?.firstOrNull()?.href,
                        contentDescription = "",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(36.dp),
                    )
                     */
                }
            },
            headlineContent = {
                Text(text = uiState.feed.dataOrNull()?.metadata?.description ?: "")
            },
            supportingContent = {
                uiState.feed.dataOrNull()?.metadata?.subtitle?.let { subtitle ->
                    Text(text = subtitle)
                }
            },
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .defaultItemPadding(),
        ) {
            RespectQuickActionButton(
                modifier = Modifier.testTag("share_btn"),
                labelText = stringResource(Res.string.share),
                iconContent = {
                    Icon(Icons.Filled.Share, null)
                },
                onClick = onClickShare
            )

            RespectQuickActionButton(
                modifier = Modifier.testTag("copy_btn"),
                labelText = stringResource(Res.string.copy_playlist),
                iconContent = {
                    Icon(Icons.Filled.ContentCopy, null)
                },
                onClick = onClickCopy
            )

            RespectQuickActionButton(
                modifier = Modifier.testTag("header_assign_btn"),
                labelText = stringResource(Res.string.assign),
                iconContent = {
                    Icon(Icons.Filled.Task, null)
                },
                onClick = onClickAssign
            )

            if (uiState.isTeacherOrAdmin) {
                RespectQuickActionButton(
                    modifier = Modifier.testTag("delete_btn"),
                    labelText = stringResource(Res.string.delete),
                    iconContent = {
                        Icon(Icons.Filled.Delete, null)
                    },
                    onClick = onClickDelete,
                )
            }
        }
    }
}