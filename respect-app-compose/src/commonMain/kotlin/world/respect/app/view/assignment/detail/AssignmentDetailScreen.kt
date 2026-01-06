package world.respect.app.view.assignment.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlinx.datetime.TimeZone
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.RespectDetailField
import world.respect.app.components.defaultItemPadding
import world.respect.datalayer.DataLoadingState
import world.respect.datalayer.ext.dataOrNull
import world.respect.datalayer.school.model.AssignmentLearningUnitRef
import world.respect.lib.opds.model.findIcons
import world.respect.libutil.ext.resolve
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.assignment_tasks
import world.respect.shared.generated.resources.due_date
import world.respect.shared.generated.resources.students
import world.respect.shared.generated.resources.clazz
import world.respect.shared.util.rememberFormattedDateTime
import world.respect.shared.viewmodel.app.appstate.getTitle
import world.respect.shared.viewmodel.assignment.detail.AssignmentDetailUiState
import world.respect.shared.viewmodel.assignment.detail.AssignmentDetailViewModel

@Composable
fun AssignmentDetailScreen(
    viewModel: AssignmentDetailViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    AssignmentDetailScreen(
        uiState = uiState,
        onClickLearningUnit = viewModel::onClickLearningUnit,
    )
}

@Composable
fun AssignmentDetailScreen(
    uiState: AssignmentDetailUiState,
    onClickLearningUnit: (AssignmentLearningUnitRef) -> Unit,
) {
    val assignment = uiState.assignment.dataOrNull()
    val dueDateFormatted = rememberFormattedDateTime(
        timeInMillis = assignment?.deadline?.toEpochMilliseconds() ?: 0,
        timeZoneId = TimeZone.currentSystemDefault().id,
    )

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item("description") {
            Text(
                text = assignment?.description ?: "",
                modifier = Modifier.fillMaxWidth().defaultItemPadding(),
            )
        }

        if(assignment?.deadline != null) {
            item("deadline") {
                RespectDetailField(
                    modifier = Modifier.fillMaxWidth().defaultItemPadding(),
                    label = {
                        Text(stringResource(Res.string.due_date))
                    },
                    value = {
                        Text(dueDateFormatted)
                    }
                )
            }
        }

        uiState.assignmentClass.dataOrNull()?.also { clazz ->
            item("class_name") {
                RespectDetailField(
                    modifier = Modifier.fillMaxWidth().defaultItemPadding(),
                    value = {
                        Text(clazz.title)
                    },
                    label = {
                        Text(stringResource(Res.string.clazz))
                    }
                )
            }
        }

        item("tasks_header") {
            Text(
                text = stringResource(Res.string.assignment_tasks),
                modifier = Modifier.defaultItemPadding(),
            )
        }

        assignment?.learningUnits?.forEachIndexed { index, learningUnitRef ->
            item(learningUnitRef.learningUnitManifestUrl.toString() + "_$index"){
                val learningUnitInfoFlow = remember(
                    uiState.learningUnitInfoFlow, learningUnitRef.learningUnitManifestUrl
                ) {
                    uiState.learningUnitInfoFlow(learningUnitRef.learningUnitManifestUrl)
                }

                val learningUnitInfo by learningUnitInfoFlow.collectAsState(DataLoadingState())

                ListItem(
                    modifier = Modifier.clickable {
                        onClickLearningUnit(learningUnitRef)
                    },
                    headlineContent = {
                        Text(learningUnitInfo.dataOrNull()?.metadata?.title?.getTitle() ?: "")
                    },
                    leadingContent = {
                        val iconLink = learningUnitInfo.dataOrNull()?.findIcons()?.firstOrNull()
                        val iconUrl = iconLink?.let {
                            learningUnitRef.learningUnitManifestUrl.resolve(it.href)
                        }
                        iconUrl?.also {
                            AsyncImage(
                                modifier = Modifier.size(40.dp),
                                model = iconUrl.toString(),
                                contentDescription = iconLink.title,
                            )
                        }
                    },
                )
            }
        }

        item("students_header") {
            Text(
                text = stringResource(Res.string.students),
                modifier = Modifier.defaultItemPadding(),
            )
        }

    }
}