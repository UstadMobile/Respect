package world.respect.app.view.studentgrouping.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.material3.Text
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.RespectBasicAlertDialog
import world.respect.app.components.RespectPersonAvatar
import world.respect.app.components.defaultItemPadding
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.cancel
import world.respect.shared.generated.resources.delete
import world.respect.shared.generated.resources.permanently_deleted
import world.respect.shared.generated.resources.permanently_delete_this_group
import world.respect.shared.generated.resources.student
import world.respect.shared.generated.resources.students
import world.respect.shared.viewmodel.studentgrouping.detail.StudentGroupingDetailUiState
import world.respect.shared.viewmodel.studentgrouping.detail.StudentGroupingDetailViewModel


@Composable
fun StudentGroupingDetailScreen(
    viewModel: StudentGroupingDetailViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    StudentGroupingDetailScreen(
        uiState = uiState,
        onClickDelete = viewModel::onClickDeleteGroup,
    )

    if (uiState.showDeleteGroupDialog) {
        RespectBasicAlertDialog(
            headlineText = stringResource(Res.string.permanently_deleted),
            bodyText = stringResource(Res.string.permanently_delete_this_group),
            onConfirm = viewModel::onConfirmDeleteGroup,
            onDismissRequest = viewModel::onDismissDeleteGroupDialog,
            confirmText = stringResource(Res.string.delete),
            dismissText = stringResource(Res.string.cancel),
        )
    }
}

@Composable
fun StudentGroupingDetailScreen(
    uiState: StudentGroupingDetailUiState = StudentGroupingDetailUiState(),
    onClickDelete: () -> Unit = {},
) {

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {
        item {
            ListItem(
                leadingContent = {
                    Box(modifier = Modifier.width(40.dp)) {
                        val displayMembers = uiState.groupMembers.take(3)
                        displayMembers.forEachIndexed { i, name ->
                            Box(
                                modifier = Modifier
                                    .offset(x = (i * 12).dp)
                                    .zIndex((displayMembers.size - i).toFloat())
                            ) {
                                RespectPersonAvatar(
                                    name = name,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                },
                headlineContent = {
                    val label = if (uiState.groupMembers.size > 1)
                        stringResource(Res.string.students)
                    else
                        stringResource(Res.string.student)
                    Text(
                        text = "${uiState.groupMembers.size} $label",
                    )
                }
            )

        }

        item("divider1") {
            HorizontalDivider()
        }

        item {
            Column(
                modifier = Modifier.fillMaxWidth()
                    .testTag("delete_group_btn")
                    .defaultItemPadding()
                    .clickable { onClickDelete() }
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(Res.string.delete)
                )
                Text(stringResource(Res.string.delete))
            }
        }

        item("divider2") {
            HorizontalDivider()
        }

        items(uiState.groupMembers.size) { index ->
            ListItem(
                modifier = Modifier.fillMaxWidth(),
                leadingContent = {
                    RespectPersonAvatar(name = uiState.groupMembers[index])
                },
                headlineContent = {
                    Text(text = uiState.groupMembers[index])
                }
            )
        }
    }
}