package world.respect.app.view.assignment.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import coil3.compose.AsyncImage
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.datetime.TimeZone
import world.respect.app.components.respectPagingItems
import world.respect.app.components.respectRememberPager
import world.respect.datalayer.DataLoadingState
import world.respect.datalayer.ext.dataOrNull
import world.respect.datalayer.school.AssignmentDataSource
import world.respect.datalayer.school.model.Assignment
import world.respect.libutil.ext.resolve
import world.respect.shared.util.rememberFormattedDateTime
import world.respect.shared.viewmodel.assignment.list.AssignmentListUiState
import world.respect.shared.viewmodel.assignment.list.AssignmentListViewModel


@Composable
fun AssignmentListScreen(
    viewModel: AssignmentListViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    AssignmentListScreen(
        uiState = uiState,
        onClickAssignment = viewModel::onClickAssignment,
    )
}

@Composable
fun AssignmentListScreen(
    uiState: AssignmentListUiState,
    onClickAssignment: (Assignment) -> Unit = { },
) {
    val pager = respectRememberPager(uiState.assignments)
    val lazyPagingItems = pager.flow.collectAsLazyPagingItems()

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        respectPagingItems(
            items = lazyPagingItems,
            key = {item, index -> item?.uid ?: index.toString() },
            contentType = { AssignmentDataSource.ENDPOINT_NAME }
        ) { assignment ->
            ListItem(
                modifier = Modifier.clickable {
                    assignment?.also(onClickAssignment)
                },
                headlineContent = {
                    Text(assignment?.title ?: "")
                },
                supportingContent = {
                    val dueDateStr = rememberFormattedDateTime(
                        timeInMillis = assignment?.deadline?.toEpochMilliseconds() ?: 0,
                        timeZoneId = TimeZone.currentSystemDefault().id,
                    )

                    assignment?.deadline?.also {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                modifier = Modifier.size(16.dp),
                                contentDescription = null
                            )

                            Spacer(Modifier.width(8.dp))
                            Text(dueDateStr)
                        }
                    }
                },
                leadingContent = {
                    val firstLearningUnit = assignment?.learningUnits?.firstOrNull()
                    val learningUnitFlow = remember(
                        firstLearningUnit?.learningUnitManifestUrl, uiState.learningUnitInfoFlow
                    ) {
                        firstLearningUnit?.learningUnitManifestUrl?.let {
                            uiState.learningUnitInfoFlow(it)
                        } ?: emptyFlow()
                    }
                    val learningUnitInfo by learningUnitFlow.collectAsState(DataLoadingState())
                    val iconLink = learningUnitInfo.dataOrNull()?.images?.firstOrNull()
                    val manifestUrl = firstLearningUnit?.learningUnitManifestUrl
                    if (iconLink != null && manifestUrl != null) {
                        AsyncImage(
                            model = manifestUrl.resolve(iconLink.href).toString(),
                            contentDescription = iconLink.title,
                            modifier = Modifier.size(40.dp),
                        )
                    }else {
                        Spacer(Modifier.size(40.dp))
                    }
                }
            )
        }
    }
}
