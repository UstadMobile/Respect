package world.respect.app.view.studentgrouping.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.material3.Text
import world.respect.app.components.RespectPersonAvatar
import world.respect.shared.viewmodel.studentgrouping.detail.StudentGroupingDetailUiState
import world.respect.shared.viewmodel.studentgrouping.detail.StudentGroupingDetailViewModel


@Composable
fun StudentGroupingDetailScreen(
    viewModel: StudentGroupingDetailViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    StudentGroupingDetailScreen(
        uiState = uiState,
        onClickBack = viewModel::onClickBack
    )
}

@Composable
fun StudentGroupingDetailScreen(
    uiState: StudentGroupingDetailUiState,
    onClickBack: () -> Unit = {}
) {

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {
        item {

            ListItem(
                modifier = Modifier.clickable {},
                leadingContent = {
                    Icon(Icons.Default.Groups, contentDescription = "")
                },
                headlineContent = {
                    Text(
                        text = "${uiState.groupMembers.size} Students",
                    )
                },
                supportingContent = {
                    //number of students in the group
                }
            )

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