package world.respect.app.view.assignment.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import coil3.compose.AsyncImage
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import world.respect.app.components.respectPagingItems
import world.respect.app.components.respectRememberPager
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.DataLoadingState
import world.respect.datalayer.ext.dataOrNull
import world.respect.datalayer.school.AssignmentDataSource
import world.respect.datalayer.school.model.Assignment
import world.respect.lib.opds.model.OpdsPublication
import world.respect.libutil.ext.resolve
import world.respect.shared.util.AssignmentFilter
import world.respect.shared.viewmodel.assignment.list.AssignmentListUiState
import world.respect.shared.viewmodel.assignment.list.AssignmentListViewModel


@Composable
fun AssignmentListScreen(
    viewModel: AssignmentListViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    AssignmentListScreen(
        uiState = uiState,
        onFilterSelected = viewModel::onFilterChanged,
        onClickAssignment = viewModel::onClickAssignment,
    )
}

@Composable
fun AssignmentListScreen(
    uiState: AssignmentListUiState,
    onFilterSelected: (AssignmentFilter) -> Unit,
    onClickAssignment: (Assignment) -> Unit = { },
) {
    val pager = respectRememberPager(uiState.assignments)
    val lazyPagingItems = pager.flow.collectAsLazyPagingItems()
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AssignmentFilter.entries.forEach { filter ->
                val label = if (filter == AssignmentFilter.ALL) {
                    filter.displayName
                } else {
                    val count = when (filter) {
                        AssignmentFilter.COMPLETED -> uiState.completedCount
                        AssignmentFilter.PENDING -> uiState.totalCount - uiState.completedCount
                        else -> 0
                    }
                    "${filter.displayName} ($count)"
                }
                FilterChip(
                    selected = uiState.selectedFilter == filter,
                    onClick = { onFilterSelected(filter) },
                    label = { Text(label) },
                    shape = RoundedCornerShape(50)
                )
            }
        }
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            respectPagingItems(
                items = lazyPagingItems,
                key = { item, index -> item?.uid ?: index.toString() },
                contentType = { AssignmentDataSource.ENDPOINT_NAME }
            ) { assignment ->
                if (assignment == null) return@respectPagingItems

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onClickAssignment(assignment) }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy((-12).dp) // Negative space for overlap
                    ) {
                        assignment.learningUnits.take(3).forEach { unit ->
                            AssignmentLearningUnitIcon(
                                manifestUrl = unit.learningUnitManifestUrl,
                                learningUnitInfoFlow = uiState.learningUnitInfoFlow
                            )
                        }
                    }

                    Spacer(Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = assignment.title,
                            style = MaterialTheme.typography.titleMedium
                        )

                        val dueDateStr = remember(assignment.deadline) {
                            assignment.deadline?.let { instant ->
                                val localDate = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
                                "${localDate.day.toString().padStart(2, '0')}/" +
                                        "${localDate.month.number.toString().padStart(2, '0')}/" +
                                        "${localDate.year}"
                            } ?: ""
                        }

                        if (uiState.isStudent) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = Color.Gray
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = dueDateStr,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                                Spacer(Modifier.width(16.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.TaskAlt,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = Color.Gray
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = "${uiState.completedCount}/${uiState.totalCount} task completed",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                            }
                            Text(
                                text = "Assigned to:  ${uiState.personName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CalendarMonth, null, Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(dueDateStr, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    if (uiState.isStudent) {
                        Text(
                            text = "98%",
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFAED581))
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White
                        )
                    } else {
                        Row(
                            modifier = Modifier.weight(0.8f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color.Gray
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(uiState.className, style = MaterialTheme.typography.bodySmall)
                        }

                        Row(
                            modifier = Modifier
                                .weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrackChanges,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color.Gray
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "${uiState.completedCount}/${uiState.totalCount} student completed",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AssignmentLearningUnitIcon(
    manifestUrl: Url,
    learningUnitInfoFlow: (Url) -> Flow<DataLoadState<OpdsPublication>>,
    modifier: Modifier = Modifier
) {
    val infoFlow = remember(manifestUrl) { learningUnitInfoFlow(manifestUrl) }
    val state by infoFlow.collectAsState(DataLoadingState())
    val iconLink = state.dataOrNull()?.images?.firstOrNull()

    if (iconLink != null) {
        AsyncImage(
            model = manifestUrl.resolve(iconLink.href).toString(),
            contentDescription = iconLink.title,
            modifier = modifier
                .size(32.dp)
                .clip(RoundedCornerShape(4.dp))
        )
    } else {
        Spacer(modifier.size(32.dp))
    }
}
