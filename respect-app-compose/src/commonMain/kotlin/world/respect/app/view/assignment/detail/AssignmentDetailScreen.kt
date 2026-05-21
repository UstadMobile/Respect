package world.respect.app.view.assignment.detail

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.defaultItemPadding
import world.respect.datalayer.school.model.AssignmentLearningUnitRef
import world.respect.lib.dataloadstate.DataLoadingState
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.lib.opds.model.findIcons
import world.respect.lib.xapi.ext.calculatePercentage
import world.respect.lib.xapi.ext.personName
import world.respect.lib.xapi.ext.personUid
import world.respect.libutil.ext.resolve
import world.respect.shared.domain.xapi.assignmentDeadline
import world.respect.shared.domain.xapi.assignmentDescription
import world.respect.shared.domain.xapi.getUnitTitle
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.assigned_to
import world.respect.shared.generated.resources.average
import world.respect.shared.generated.resources.deadline
import world.respect.shared.generated.resources.no_student_data_available
import world.respect.shared.util.AssignmentStatusFilter
import world.respect.shared.viewmodel.app.appstate.getTitle
import world.respect.shared.viewmodel.assignment.detail.AssignmentDetailUiState
import world.respect.shared.viewmodel.assignment.detail.AssignmentDetailViewModel
import kotlin.math.roundToInt

private const val NAME_COLUMN_WIDTH = 120
private const val TASK_COLUMN_WIDTH = 80
private const val HEADER_HEIGHT = 160

@Composable
fun AssignmentDetailScreen(
    viewModel: AssignmentDetailViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    AssignmentDetailScreen(
        uiState = uiState,
        onStatusFilterChanged = viewModel::onStatusFilterChanged,
        onClickTask = viewModel::onClickTask
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AssignmentDetailScreen(
    uiState: AssignmentDetailUiState,
    onStatusFilterChanged: (AssignmentStatusFilter) -> Unit = { },
    onClickTask: (AssignmentLearningUnitRef) -> Unit = { },
) {
    val horizontalScrollState = rememberScrollState()

    Column(modifier = Modifier.fillMaxSize()) {
        if (!uiState.isFullscreen) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultItemPadding()
            ) {
                Text(
                    text = uiState.xApiStatement.dataOrNull()?.assignmentDescription ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(Res.string.deadline),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                        Text(
                            text = uiState.xApiStatement.dataOrNull()?.assignmentDeadline?.toString() ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(Res.string.assigned_to),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                        Text(
                            text = if (uiState.isStudent) uiState.assigneeStudentName else uiState.xApiStatement.dataOrNull()?.actor?.name.orEmpty(),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultItemPadding(top = 16.dp, bottom = 4.dp),
                    thickness = 1.dp
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = if (uiState.isFullscreen) 8.dp else 0.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AssignmentStatusFilter.entries.forEach { filter ->
                val count = uiState.statusCounts[filter] ?: 0
                FilterChip(
                    selected = uiState.selectedStatusFilter == filter,
                    onClick = { onStatusFilterChanged(filter) },
                    label = {
                        Text(
                            text = "${filter.displayName} ($count)",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    },
                    shape = RoundedCornerShape(50)
                )
            }
        }

        if (uiState.isStudent) {
            val units = uiState.tasks
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .defaultItemPadding(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(units) { unit ->
                    StudentLearningUnitItem(
                        unit = unit,
                        uiState = uiState,
                        onClick = { onClickTask(unit) }
                    )
                }
            }
        } else {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                val nameColWidth = minOf(maxWidth / 3, (NAME_COLUMN_WIDTH).dp)
                val taskColWidth = (TASK_COLUMN_WIDTH).dp
                val headerHeight = minOf(maxHeight / 2, (HEADER_HEIGHT).dp)

                val assignmentResults = uiState.rowsToDisplay

                if (assignmentResults.isNotEmpty()) {
                    val progressMap = uiState.allProgressMap
                    val tasks = uiState.tasks

                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        // STICKY HEADER: Task Icons and Names
                        stickyHeader {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(headerHeight),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Spacer(
                                    Modifier
                                        .width(nameColWidth)
                                        .height(headerHeight)
                                )

                                // Scrollable Task Headers
                                Row(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .horizontalScroll(horizontalScrollState),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    tasks.forEach { unit ->
                                        TaskHeaderCell(
                                            unit,
                                            uiState,
                                            taskColWidth,
                                            headerHeight
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .width(taskColWidth)
                                            .height(headerHeight),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = stringResource(Res.string.average),
                                            modifier = Modifier.rotate(-90f),
                                            textAlign = TextAlign.Center,
                                            style = MaterialTheme.typography.labelSmall,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }

                        items(assignmentResults, key = { it.personUid }) { student ->
                            Row(modifier = Modifier.fillMaxWidth()) {
                                // Fixed Student Name Column
                                StudentNameCell(student.personName, nameColWidth)
                                // Scrollable Grades/Progress Cells
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(horizontalScrollState)
                                ) {
                                    tasks.forEach { unit ->
                                        val progress =
                                            progressMap[student.personUid]?.get(unit.learningUnitManifestUrl.toString())
                                        GradeCell(progress?.calculatePercentage(), taskColWidth)
                                    }
                                    // Average Score Cell
                                    AverageCell(
                                        uiState.getAverageForStudent(student.personUid),
                                        taskColWidth
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(Res.string.no_student_data_available),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StudentLearningUnitItem(
    unit: AssignmentLearningUnitRef,
    uiState: AssignmentDetailUiState,
    onClick: () -> Unit
) {
    val infoFlow =
        remember(unit.learningUnitManifestUrl) { uiState.taskInfoFlow(unit.learningUnitManifestUrl) }
    val state by infoFlow.collectAsState(DataLoadingState())
    val publication = state.dataOrNull()
    val iconLink = publication?.images?.firstOrNull()

    // Use actual title from publication metadata if available, otherwise from xAPI statement
    val title = uiState.xApiStatement.dataOrNull()?.getUnitTitle(unit.learningUnitManifestUrl.toString())
        ?: publication?.metadata?.title?.getTitle()
        ?: "Loading..."

    val progress =
        remember(uiState.assignmentProgressList, unit.learningUnitManifestUrl, uiState.personGuid) {
            uiState.assignmentProgressList
                .find { it.personUid == uiState.personGuid }
                ?.progress
                ?.find { it.activityId == unit.learningUnitManifestUrl.toString() }
        }

    val percent = progress?.calculatePercentage() ?: 0
    val (bgColor, textColor) = when {
        percent >= 90 -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        percent > 0 -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        else -> MaterialTheme.colorScheme.surfaceContainer to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(contentAlignment = Alignment.BottomStart) {
            if (iconLink != null) {
                AsyncImage(
                    model = unit.learningUnitManifestUrl.resolve(iconLink.href).toString(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(bgColor, RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        title.lastOrNull()?.toString() ?: "?",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }
            if (percent > 90) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.surfaceContainer,
                    modifier = Modifier.size(16.dp).background(Color.White, CircleShape)
                        .padding(1.dp)
                )
            }
        }

        Spacer(Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Face,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = Color.Gray
                )
                Spacer(Modifier.width(4.dp))
            }
        }

        if (percent < 60) {
            Box(
                modifier = Modifier.size(32.dp).background(Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$percent%",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 8.sp
                )
            }
        } else {
            Text(
                text = "$percent%",
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(bgColor)
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelMedium,
                color = textColor
            )
        }
    }
}

@Composable
fun TaskHeaderCell(
    unit: AssignmentLearningUnitRef,
    uiState: AssignmentDetailUiState,
    width: Dp,
    height: Dp
) {
    val info by uiState.taskInfoFlow(unit.learningUnitManifestUrl)
        .collectAsState(DataLoadingState())

    val title = uiState.xApiStatement.dataOrNull()?.getUnitTitle(unit.learningUnitManifestUrl.toString())
        ?: info.dataOrNull()?.metadata?.title?.getTitle()
        ?: ""

    Column(
        modifier = Modifier
            .width(width)
            .height(height),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.rotate(-90f),
                textAlign = TextAlign.Start,
                style = MaterialTheme.typography.labelSmall
            )
        }
        val iconUrl = info.dataOrNull()?.findIcons()?.firstOrNull()?.let {
            unit.learningUnitManifestUrl.resolve(it.href).toString()
        }

        if (iconUrl != null) {
            AsyncImage(
                model = iconUrl,
                contentDescription = null,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
fun StudentNameCell(name: String, width: Dp) {
    Box(
        modifier = Modifier
            .width(width)
            .height(48.dp)
            .padding(8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        MaterialTheme.colorScheme.onSurfaceVariant,
                        shape = MaterialTheme.shapes.small
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name.firstOrNull()?.toString() ?: "?",
                    color = MaterialTheme.colorScheme.surface,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun GradeCell(percent: Int?, width: Dp) {
    val (bgColor, textColor) = when {
        percent == null -> MaterialTheme.colorScheme.surfaceContainer to MaterialTheme.colorScheme.onSurfaceVariant
        percent >= 90 -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.primary
        percent > 0 -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.surfaceContainer to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Box(
        modifier = Modifier
            .width(width)
            .height(48.dp)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        if (percent == null) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("--", color = textColor, style = MaterialTheme.typography.bodySmall)
            }
        } else {
            Box(
                modifier = Modifier
                    .background(bgColor, shape = MaterialTheme.shapes.small)
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("${percent}%", color = textColor, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun AverageCell(avg: Double?, width: Dp) {
    val (bgColor, textColor) = when {
        avg == null -> MaterialTheme.colorScheme.surfaceContainer to MaterialTheme.colorScheme.onSurfaceVariant
        avg >= 90 -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.primary
        avg > 0 -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.surfaceContainer to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Box(
        modifier = Modifier
            .width(width)
            .height(48.dp),
        contentAlignment = Alignment.Center
    ) {
        if (avg == null) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("--", color = textColor, style = MaterialTheme.typography.bodySmall)
            }
        } else {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(bgColor, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${avg.roundToInt()}%",
                    color = textColor,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
