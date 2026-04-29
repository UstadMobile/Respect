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
import androidx.compose.foundation.layout.wrapContentWidth
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
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import world.respect.datalayer.DataLoadingState
import world.respect.datalayer.ext.dataOrNull
import world.respect.datalayer.school.model.AssignmentLearningUnitRef
import world.respect.lib.opds.model.findIcons
import world.respect.libutil.ext.resolve
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.assigned_to
import world.respect.shared.generated.resources.deadline
import world.respect.shared.util.AssignmentStatusFilter
import world.respect.shared.viewmodel.app.appstate.getTitle
import world.respect.shared.viewmodel.assignment.detail.AssignmentDetailUiState
import world.respect.shared.viewmodel.assignment.detail.AssignmentDetailViewModel
import java.util.Locale

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
        onClickLearningUnit = viewModel::onClickLearningUnit
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AssignmentDetailScreen(
    uiState: AssignmentDetailUiState,
    onStatusFilterChanged: (AssignmentStatusFilter) -> Unit = { },
    onClickLearningUnit: (AssignmentLearningUnitRef) -> Unit = { },
) {
    val assignment = uiState.assignment.dataOrNull()
    val horizontalScrollState = rememberScrollState()

    Column(modifier = Modifier.fillMaxSize()) {
        if (!uiState.isFullscreen) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = assignment?.description ?: "",
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
                        val dueDateStr = remember(assignment?.deadline) {
                            assignment?.deadline?.let { instant ->
                                val localDate =
                                    instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
                                "${localDate.day.toString().padStart(2, '0')}/" +
                                        "${localDate.month.number.toString().padStart(2, '0')}/" +
                                        "${localDate.year}"
                            } ?: ""
                        }
                        Text(
                            text = dueDateStr,
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
                        val assignedTo = if (uiState.isStudent) uiState.personName else uiState.assignmentClass.dataOrNull()?.title ?: "-"
                        Text(
                            text = assignedTo,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 4.dp),
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
                FilterChip(
                    selected = uiState.selectedStatusFilter == filter,
                    onClick = { onStatusFilterChanged(filter) },
                    label = {
                        Text(
                            filter.displayName,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    },
                    shape = RoundedCornerShape(50)
                )
            }
        }

        if (uiState.isStudent) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val units = assignment?.learningUnits ?: emptyList()
                items(units) { unit ->
                    StudentLearningUnitItem(
                        unit = unit,
                        uiState = uiState,
                        onClick = { onClickLearningUnit(unit) }
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

                val students = remember(uiState.assignmentProgressRow) {
                    uiState.assignmentProgressRow.distinctBy { it.personUid }
                }
                val progressMap = remember(uiState.assignmentProgressRow) {
                    uiState.assignmentProgressRow.groupBy { it.personUid }
                        .mapValues { entry -> entry.value.associateBy { it.activityId } }
                }

                val units = assignment?.learningUnits ?: emptyList()

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    // STICKY HEADER: Task Icons and Names
                    stickyHeader {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(headerHeight)
                                .background(color = MaterialTheme.colorScheme.surface)
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
                                    .horizontalScroll(horizontalScrollState)
                            ) {
                                units.forEach { unit ->
                                    TaskHeaderCell(unit, uiState, taskColWidth, headerHeight)
                                }
                                Box(
                                    modifier = Modifier
                                        .width(taskColWidth)
                                        .height(headerHeight),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(
                                        text = "Average",
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

                    when {
                        students.isNotEmpty() -> {
                            items(students, key = { it.personUid }) { student ->
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    // Fixed Student Name Column
                                    StudentNameCell(student.personName ?: "Unknown", nameColWidth)
                                    // Scrollable Grades/Progress Cells
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(horizontalScrollState)
                                    ) {
                                        units.forEach { unit ->
                                            val progress = progressMap[student.personUid]?.get(unit.learningUnitManifestUrl.toString())
                                            val percent = progress?.progress ?: progress?.scoreScaled?.let { (it * 100).toInt() }
                                            GradeCell(percent, taskColWidth)
                                        }
                                        // Average Score Cell
                                        val studentProgressValues = progressMap[student.personUid]?.values?.mapNotNull { 
                                            it.progress ?: it.scoreScaled?.let { s -> (s * 100).toInt() }
                                        }
                                        val avg = if (!studentProgressValues.isNullOrEmpty()) {
                                            studentProgressValues.average()
                                        } else null
                                        AverageCell(avg, taskColWidth)
                                    }
                                }
                            }
                        }

                        else -> {
                            item("empty_state") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No student data available",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
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
    val infoFlow = remember(unit.learningUnitManifestUrl) { uiState.learningUnitInfoFlow(unit.learningUnitManifestUrl) }
    val state by infoFlow.collectAsState(DataLoadingState())
    val publication = state.dataOrNull()
    val iconLink = publication?.images?.firstOrNull()

    // Use actual title from publication metadata if available
    val title = publication?.metadata?.title?.getTitle() ?: "Loading..."
    
    val progress = remember(uiState.assignmentProgressRow, unit.learningUnitManifestUrl, uiState.personGuid) {
        uiState.assignmentProgressRow.find { 
            it.personUid == uiState.personGuid && it.activityId == unit.learningUnitManifestUrl.toString() 
        }
    }
    
    val percent = progress?.progress ?: progress?.scoreScaled?.let { (it * 100).toInt() } ?: 0
    val color = when {
        percent >= 90 -> Color(0xFFAED581) // TODO NEED TO CHANGE HARDCODED
        percent > 0 -> Color(0xFFD48245)
        else -> Color.LightGray
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
                        .background(color, RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(title.lastOrNull()?.toString() ?: "?", color = Color.White, style = MaterialTheme.typography.headlineSmall)
                }
            }
            if (percent > 90) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFFAED581),
                    modifier = Modifier.size(16.dp).background(Color.White, CircleShape).padding(1.dp)
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
                Text(text = "$percent%", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp)
            }
        } else {
            Text(
                text = "$percent%",
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelMedium,
                color = Color.White
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
    val info by uiState.learningUnitInfoFlow(unit.learningUnitManifestUrl)
        .collectAsState(DataLoadingState())

    Column(
        modifier = Modifier
            .width(width)
            .height(height)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Box(
            modifier = Modifier
                .width(width)
                .wrapContentWidth(unbounded = true),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = info.dataOrNull()?.metadata?.title?.getTitle() ?: "",
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.rotate(-90f)
            )
        }
        Box(
            modifier = Modifier
                .size(40.dp),
            contentAlignment = Alignment.Center
        ) {
            val iconUrl = info.dataOrNull()?.findIcons()?.firstOrNull()?.let {
                unit.learningUnitManifestUrl.resolve(it.href).toString()
            }

            if (iconUrl != null) {
                AsyncImage(
                    model = iconUrl,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun StudentNameCell(name: String, width: Dp) {
    Box(
        modifier = Modifier
            .width(width)
            .fillMaxHeight()
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
            .fillMaxHeight()
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        if (percent == null) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("--", color = textColor, style = MaterialTheme.typography.bodySmall)
            }
        } else {
            Box(
                modifier = Modifier
                    .background(bgColor, shape = MaterialTheme.shapes.small)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
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
            .fillMaxHeight(),
        contentAlignment = Alignment.Center
    ) {
        if (avg == null) {
            Text("--", color = textColor, style = MaterialTheme.typography.bodySmall)
        } else {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(bgColor, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    String.format(Locale.US, "%.1f%%", avg),
                    color = textColor,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
