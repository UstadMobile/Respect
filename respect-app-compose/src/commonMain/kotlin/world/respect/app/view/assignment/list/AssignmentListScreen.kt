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
import androidx.compose.foundation.lazy.items
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
import coil3.compose.AsyncImage
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.defaultItemPadding
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.DataLoadingState
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.lib.opds.model.OpdsPublication
import world.respect.lib.xapi.model.AssignmentSummary
import world.respect.libutil.ext.resolve
import world.respect.libutil.util.time.toDisplayDateString
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.assigned_to
import world.respect.shared.generated.resources.calendar_month
import world.respect.shared.generated.resources.change
import world.respect.shared.generated.resources.menu
import world.respect.shared.generated.resources.percentage_format
import world.respect.shared.generated.resources.student_completed
import world.respect.shared.generated.resources.task
import world.respect.shared.generated.resources.task_completed
import world.respect.shared.generated.resources.track_changes
import world.respect.shared.util.AssignmentListScreenFilter
import world.respect.shared.viewmodel.assignment.list.AssignmentListUiState
import world.respect.shared.viewmodel.assignment.list.AssignmentListViewModel
import kotlin.math.roundToInt


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
    onFilterSelected: (AssignmentListScreenFilter) -> Unit,
    onClickAssignment: (AssignmentSummary) -> Unit = { },
) {
    val assignments = uiState.assignments.dataOrNull() ?: emptyList()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultItemPadding(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AssignmentListScreenFilter.entries.forEach { filter ->
                FilterChip(
                    selected = uiState.selectedFilter == filter,
                    onClick = { onFilterSelected(filter) },
                    label = { Text(uiState.getLabelForFilter(filter)) },
                    shape = RoundedCornerShape(50)
                )
            }
        }
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(
                items = assignments,
            ) { summary ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onClickAssignment(summary) }
                        .defaultItemPadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy((-12).dp)
                    ) {
                        summary.learningUnitManifestUrls.take(1).forEach { manifestUrlStr ->
                            val manifestUrl = remember(manifestUrlStr) { Url(manifestUrlStr) }
                            AssignmentLearningUnitIcon(
                                manifestUrl = manifestUrl,
                                learningUnitInfoFlow = uiState.learningUnitInfoFlow
                            )
                        }
                    }

                    Spacer(Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = summary.title,
                            style = MaterialTheme.typography.titleMedium
                        )

                        val dueDateStr = remember(summary.deadline) {
                            summary.deadline?.toDisplayDateString() ?: ""
                        }

                        if (uiState.isStudent) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = stringResource(Res.string.calendar_month),
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
                                        contentDescription = stringResource(Res.string.task),
                                        modifier = Modifier.size(14.dp),
                                        tint = Color.Gray
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = "${summary.completedCount}/${summary.totalCount}" + stringResource(
                                            Res.string.task_completed
                                        ),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                            }
                            Text(
                                text = stringResource(Res.string.assigned_to) + uiState.personName,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.CalendarMonth,
                                    contentDescription = stringResource(Res.string.calendar_month),
                                    Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(dueDateStr, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    if (uiState.isStudent) {
                        val percent = if (summary.totalCount > 0) {
                            (summary.completedCount.toFloat() / summary.totalCount.toFloat() * 100).toInt()
                        } else {
                            0
                        }
                        Text(
                            text = stringResource(Res.string.percentage_format, percent),
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainer)
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
                                contentDescription = stringResource(Res.string.menu),
                                modifier = Modifier.size(16.dp),
                                tint = Color.Gray
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(summary.assignedActor.name ?: "", style = MaterialTheme.typography.bodySmall)
                        }

                        Row(
                            modifier = Modifier
                                .weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrackChanges,
                                contentDescription = stringResource(Res.string.track_changes),
                                modifier = Modifier.size(16.dp),
                                tint = Color.Gray
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "${summary.completedCount}/${summary.totalCount}" + stringResource(
                                    Res.string.student_completed
                                ),
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
