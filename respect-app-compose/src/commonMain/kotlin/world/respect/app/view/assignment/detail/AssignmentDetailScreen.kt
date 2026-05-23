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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import world.respect.app.components.defaultItemPadding
import world.respect.datalayer.school.xapi.ext.idStr
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.DataLoadingState
import world.respect.lib.dataloadstate.DataReadyState
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.lib.opds.model.OpdsPublication
import world.respect.lib.xapi.composites.AssignmentAndProgress
import world.respect.lib.xapi.composites.XapiActorAndAssignmentProgress
import world.respect.lib.xapi.composites.XapiAssignmentProgress
import world.respect.lib.xapi.ext.addActivityToContextActivitiesGrouping
import world.respect.lib.xapi.ext.calculatePercentage
import world.respect.lib.xapi.model.XapiAccount
import world.respect.lib.xapi.model.XapiActivity
import world.respect.lib.xapi.model.XapiAgent
import world.respect.libutil.ext.resolve
import world.respect.libutil.util.time.toDisplayDateString
import world.respect.shared.domain.xapi.assignmentDeadline
import world.respect.shared.domain.xapi.assignmentDescription
import world.respect.shared.domain.xapi.createBlankAssignmentStatement
import world.respect.shared.domain.xapi.getUnitTitle
import world.respect.shared.domain.xapi.manifestUrl
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.assigned_to
import world.respect.shared.generated.resources.average
import world.respect.shared.generated.resources.deadline
import world.respect.shared.generated.resources.no_data
import world.respect.shared.generated.resources.no_student_data_available
import world.respect.shared.generated.resources.percentage_format
import world.respect.shared.generated.resources.task_image
import world.respect.shared.generated.resources.toggle_fullscreen
import world.respect.shared.util.AssignmentStatusFilter
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
        onClickTask = viewModel::onClickTask,
        onToggleFullscreen = viewModel::onToggleFullscreen
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AssignmentDetailScreen(
    uiState: AssignmentDetailUiState,
    onStatusFilterChanged: (AssignmentStatusFilter) -> Unit = { },
    onClickTask: (XapiActivity) -> Unit = { },
    onToggleFullscreen: () -> Unit = { },
) {
    val horizontalScrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (!uiState.isFullscreen) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultItemPadding()
                ) {
                    Text(
                        text = uiState.assignmentProgress.dataOrNull()?.assignmentStatement?.assignmentDescription
                            ?: "",
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
                                text = uiState.assignmentProgress.dataOrNull()?.assignmentStatement?.assignmentDeadline?.toDisplayDateString()
                                    ?: "",
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
                                text = uiState.assignmentProgress.dataOrNull()?.assignmentStatement?.actor?.name.orEmpty(),
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
                                text = "${stringResource(filter.titleRes)} ($count)",
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
                        .defaultItemPadding(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.tasks) { task ->
                        val studentProgress =
                            uiState.assignmentProgressList.firstOrNull()

                        val progress = remember(studentProgress, task.id) {
                            studentProgress?.progress
                                ?.find { it.activityId == task.id }
                        }

                        AssignmentTaskListRow(
                            task = task,
                            taskTitle = uiState.assignmentProgress.dataOrNull()
                                ?.assignmentStatement?.getUnitTitle(task.id) ?: "",
                            onTaskClick = { onClickTask(task) },
                            progress = progress,
                            taskInfoFlow = uiState.taskInfoFlow
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
                        val tasks = uiState.tasks

                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            // STICKY HEADER: Task Icons and Names
                            stickyHeader("header") {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(headerHeight)
                                        .background(MaterialTheme.colorScheme.surface),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Spacer(
                                        Modifier.width(nameColWidth).height(headerHeight)
                                    )

                                    // Scrollable Task Headers
                                    Row(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .horizontalScroll(horizontalScrollState),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        tasks.forEach { taskActivity ->
                                            AssignmentDetailTaskHeader(
                                                activity = taskActivity,
                                                taskInfoFlow = uiState.taskInfoFlow,
                                                taskColWidth = taskColWidth,
                                                headerHeight = headerHeight
                                            )
                                        }

                                        AssignmentDetailHeaderCell(
                                            title = stringResource(Res.string.average),
                                            width = taskColWidth,
                                            height = headerHeight
                                        )
                                    }
                                }
                            }

                            itemsIndexed(
                                items = assignmentResults,
                                key = { index, item -> item.actor.idStr ?: "a_$index" },
                            ) { _, studentAndProgress ->
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    // Fixed Student Name Column
                                    StudentNameCell(
                                        name = studentAndProgress.actor.name ?: "",
                                        width = nameColWidth
                                    )

                                    // Scrollable Grades/Progress Cells
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(horizontalScrollState)
                                    ) {
                                        studentAndProgress.progress.forEach { progressItem ->
                                            AssignmentDetailStudentProgressCell(
                                                progress = progressItem,
                                                modifier = Modifier.width(taskColWidth)
                                            )
                                        }

//                                        // Average Score Cell
//                                        AverageCell(
//                                            uiState.getAverageForStudent(studentAndProgress.personUid),
//                                            taskColWidth
//                                        )
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

        if (!uiState.isStudent) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .defaultItemPadding(bottom = 64.dp),
                horizontalAlignment = Alignment.End
            ) {
                FloatingActionButton(
                    onClick = onToggleFullscreen,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("fullscreen_toggle_button"),
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    Icon(
                        imageVector = if (uiState.isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                        contentDescription = stringResource(Res.string.toggle_fullscreen)
                    )
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

// Student-side task list section on the detail screen

@Composable
fun AssignmentTaskListRow(
    task: XapiActivity,
    taskTitle: String,
    onTaskClick: () -> Unit,
    progress: XapiAssignmentProgress? = null,
    taskInfoFlow: (Url) -> Flow<DataLoadState<OpdsPublication>>,
) {
    val manifestUrl = task.manifestUrl

    val infoFlow = remember(manifestUrl) {
        manifestUrl?.let { taskInfoFlow(it) } ?: flowOf(DataLoadingState())
    }

    val state by infoFlow.collectAsState(DataLoadingState())

    val iconLink = state.dataOrNull()?.images?.firstOrNull()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onTaskClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    MaterialTheme.colorScheme.primaryContainer,
                    RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = taskTitle.firstOrNull()?.toString()?.uppercase() ?: "",
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.titleLarge
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = taskTitle,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (iconLink != null && manifestUrl != null) {
                AsyncImage(
                    model = manifestUrl.resolve(iconLink.href).toString(),
                    contentDescription = stringResource(Res.string.task_image),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
        AssignmentProgressIndicator(
            percent = progress?.calculatePercentage() ?: 0,
            isCompleted = progress?.completed == true
        )
    }
}


@Composable
fun AssignmentProgressIndicator(
    percent: Int,
    isCompleted: Boolean,
    modifier: Modifier = Modifier
) {
    if (!isCompleted) {
        Box(
            modifier = modifier
                .size(40.dp)
                .background(
                    MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            // Background track
            CircularProgressIndicator(
                progress = { 1f },
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                strokeWidth = 3.dp,
                trackColor = ProgressIndicatorDefaults.circularIndeterminateTrackColor,
                strokeCap = ProgressIndicatorDefaults.CircularDeterminateStrokeCap,
            )
            // Progress arc
            CircularProgressIndicator(
                progress = { percent / 100f },
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.dp,
                trackColor = ProgressIndicatorDefaults.circularIndeterminateTrackColor,
                strokeCap = ProgressIndicatorDefaults.CircularDeterminateStrokeCap,
            )
            // Percentage text
            Text(
                text = stringResource(Res.string.percentage_format, percent),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 10.sp
            )
        }
    } else {

        Text(
            text = stringResource(Res.string.percentage_format, percent),
            modifier = modifier
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}


@Composable
fun GradeCell(percent: Int?, width: Dp) {
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
                Text(
                    text = stringResource(Res.string.no_data),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(Res.string.percentage_format, percent),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun AverageCell(avg: Double?, width: Dp) {
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
                Text(
                    text = stringResource(Res.string.no_data),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(Res.string.percentage_format, avg.roundToInt()),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}


@Composable
@Preview
fun AssignmentDetailScreenTeacherPreview() {
    val assignmentId = "http://example.com/assignments/1"

    val assignmentTaskId1 = "http://example.app/math1"

    AssignmentDetailScreen(
        uiState = AssignmentDetailUiState(
            assignmentProgress = DataReadyState(
                data = AssignmentAndProgress(
                    assignmentStatement = createBlankAssignmentStatement(
                        assignmentActivityId = assignmentId,
                        instructor = XapiAgent(
                            name = "Alice Instructor",
                            account = XapiAccount("http://example.com", "42")
                        )
                    ).addActivityToContextActivitiesGrouping(
                        XapiActivity(
                            id = assignmentTaskId1,
                        )
                    ),
                    progress = listOf(
                        XapiActorAndAssignmentProgress(
                            actor = XapiAgent(
                                name = "Bob Student",
                                account = XapiAccount("http://example.com", "43")
                            ),
                            progress = listOf(
                                XapiAssignmentProgress(
                                    activityId = assignmentTaskId1,
                                    completed = true,
                                    successful = true,
                                    scoreScaled = 0.95f,
                                )
                            )
                        )
                    )
                )
            )
        )
    )
}
