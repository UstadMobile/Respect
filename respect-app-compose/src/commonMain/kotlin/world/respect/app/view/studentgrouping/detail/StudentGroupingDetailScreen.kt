package world.respect.app.view.studentgrouping.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.material3.Text
import androidx.compose.ui.platform.testTag
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.RespectPersonAvatar
import world.respect.app.components.defaultItemPadding
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.delete
import world.respect.shared.generated.resources.student
import world.respect.shared.viewmodel.studentgrouping.detail.StudentGroupingDetailUiState
import world.respect.shared.viewmodel.studentgrouping.detail.StudentGroupingDetailViewModel


@Composable
fun StudentGroupingDetailScreen(
    viewModel: StudentGroupingDetailViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    StudentGroupingDetailScreen(
        uiState = uiState
    )
}

@Composable
fun StudentGroupingDetailScreen(
    uiState: StudentGroupingDetailUiState
) {

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {
        item {

            ListItem(
                modifier = Modifier.clickable {},
                leadingContent = {
                    Icon(Icons.Default.Groups,
                        contentDescription = null)
                },
                headlineContent = {
                    Text(
                        text = "${uiState.groupMembers.size} " +
                                stringResource(Res.string.student),
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
            ) {
                Icon(
                    Icons.Outlined.Delete,
                    contentDescription = stringResource(Res.string.delete)
                )
                Text(stringResource(Res.string.delete))
            }
        }

        item("divider2 ") {
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
                },
            )
        }
    }
}