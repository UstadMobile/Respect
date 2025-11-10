package world.respect.app.view.enrollment.list

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.paging.compose.collectAsLazyPagingItems
import world.respect.app.components.respectPagingItems
import world.respect.app.components.respectRememberPager
import world.respect.datalayer.school.EnrollmentDataSource
import world.respect.shared.viewmodel.enrollment.list.EnrollmentListUiState
import world.respect.shared.viewmodel.enrollment.list.EnrollmentListViewModel

@Composable
fun EnrollmentListScreen(
    viewModel: EnrollmentListViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    EnrollmentListScreen(uiState = uiState)
}

@Composable
fun EnrollmentListScreen(
    uiState: EnrollmentListUiState
) {
    val pager = respectRememberPager(uiState.enrollments)
    val lazyPagingItems = pager.flow.collectAsLazyPagingItems()

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        respectPagingItems(
            items = lazyPagingItems,
            key = { item, index -> item?.uid ?: index.toString() },
            contentType = { EnrollmentDataSource.ENDPOINT_NAME },
        )
        { enrollment ->
            ListItem(
                modifier = Modifier.fillMaxWidth(),
                headlineContent = {
                    Column {
                        // First line: beginDate - endDate
                        Text(
                            text = "${enrollment?.beginDate ?: ""} - ${enrollment?.endDate ?: ""}"
                        )
                        Text(
                            text = "(${enrollment?.role})",
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                        )
                    }
                },
            )

        }
    }
}
