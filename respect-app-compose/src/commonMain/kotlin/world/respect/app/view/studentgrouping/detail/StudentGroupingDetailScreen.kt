package world.respect.app.view.studentgrouping.detail

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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

    ListItem(
        modifier = Modifier.clickable {},
        leadingContent = {
            Icon(Icons.Default.Groups, contentDescription = "")
        },
        headlineContent = {
            //description of group
        },
        supportingContent = {
            //number of students in the group
        }
    )
}
