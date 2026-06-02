package world.respect.app.view.assignment.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.DataLoadingState
import world.respect.lib.dataloadstate.NoDataLoadedState
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.lib.opds.model.OpdsPublication
import world.respect.lib.opds.model.asLangMap
import world.respect.lib.xapi.composites.XapiAssignmentTaskProgress
import world.respect.lib.xapi.ext.webPubManifestAsUrlOrNull
import world.respect.lib.xapi.model.XapiActivity
import world.respect.libutil.ext.resolve
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.task_image
import world.respect.shared.viewmodel.app.appstate.getTitle

@Composable
fun AssignmentDetailTaskListItem(
    activity: XapiActivity,
    progress: XapiAssignmentTaskProgress,
    taskInfoFlow: (Url) -> Flow<DataLoadState<OpdsPublication>>,
    onClickTask: (XapiActivity) -> Unit = { },
) {
    val title = activity.definition?.name?.asLangMap()?.getTitle()
        ?: ""

    val manifestUrl = activity.definition?.webPubManifestAsUrlOrNull()

    val taskInfoFlow = remember(
        manifestUrl, taskInfoFlow
    ) {
        manifestUrl?.let {
            taskInfoFlow(it)
        } ?: flowOf(
            NoDataLoadedState(NoDataLoadedState.Reason.NOT_FOUND)
        )
    }

    val opdsData by taskInfoFlow.collectAsState(DataLoadingState())

    val iconUrl = if(manifestUrl != null) {
        opdsData.dataOrNull()?.images?.firstOrNull()?.href?.let {
            manifestUrl.resolve(it)
        }
    }else {
        null
    }

    ListItem(
        modifier = Modifier.clickable {
            onClickTask(activity)
        },
        headlineContent = {
            Text(title)
        },
        leadingContent = {
            if (iconUrl != null) {
                AsyncImage(
                    model = iconUrl.toString(),
                    contentDescription = stringResource(Res.string.task_image),
                    modifier = Modifier.size(40.dp)
                )
            }
        },
        trailingContent = {
            AssignmentDetailStudentProgressCell(
                progress = progress,
                modifier = Modifier.size(64.dp),
            )
        }
    )
}

@Composable
@Preview
fun AssignmentDetailTaskListItemPreview() {
    AssignmentDetailTaskListItem(
        activity = mockAssignmentTaskActivity,
        progress = XapiAssignmentTaskProgress(
            activityId = mockAssignmentTaskId1,
            completed = true,
            successful = true,
            scoreScaled = 0.95f,
        ),
        taskInfoFlow = { emptyFlow() },
    )
}
