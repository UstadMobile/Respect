package world.respect.app.view.assignment.edit

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.RespectLocalDateTimeField
import world.respect.app.components.defaultItemPadding
import world.respect.app.components.uiTextStringResource
import world.respect.datalayer.DataLoadingState
import world.respect.datalayer.ext.dataOrNull
import world.respect.datalayer.school.model.Assignment
import world.respect.datalayer.school.model.AssignmentLearningUnitRef
import world.respect.datalayer.school.model.Clazz
import world.respect.lib.opds.model.findIcons
import world.respect.libutil.ext.resolve
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.assign_to
import world.respect.shared.generated.resources.assignment_title
import world.respect.shared.generated.resources.description
import world.respect.shared.generated.resources.fingerprint
import world.respect.shared.generated.resources.no_tasks_selected_yet
import world.respect.shared.generated.resources.please_click_plus_button_to_add_one
import world.respect.shared.generated.resources.required
import world.respect.shared.generated.resources.tasks
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.app.appstate.getTitle
import world.respect.shared.viewmodel.assignment.edit.AssignmentEditUiState
import world.respect.shared.viewmodel.assignment.edit.AssignmentEditViewModel

@Composable
fun AssignmentEditScreen(
    viewModel: AssignmentEditViewModel
) {
    val uiState by viewModel.uiState.collectAsState(Dispatchers.Main.immediate)

    AssignmentEditScreen(
        uiState = uiState,
        onEntityChanged = viewModel::onEntityChanged,
        onAssigneeTextChanged = viewModel::onAssigneeTextChanged,
        onClickAddLearningUnit = viewModel::onClickAddLearningUnit,
        onAssigneeClassSelected = viewModel::onAssigneeClassSelected,
        onClickRemoveLearningUnit = viewModel::onClickRemoveLearningUnit,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignmentEditScreen(
    uiState: AssignmentEditUiState,
    onEntityChanged: (Assignment) -> Unit,
    onAssigneeTextChanged: (String) -> Unit,
    onAssigneeClassSelected: (Clazz) -> Unit,
    onClickAddLearningUnit: () -> Unit,
    onClickRemoveLearningUnit: (AssignmentLearningUnitRef) -> Unit,
) {
    val assignment = uiState.assignment.dataOrNull()
    val filteredOptions = if (uiState.assigneeText.isNotBlank()) {
        uiState.classOptions.filter {
            it.title.contains(uiState.assigneeText, ignoreCase = true)
        }
    } else {
        uiState.classOptions
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth().defaultItemPadding().testTag("title"),
            value = assignment?.title ?: "",
            label = {
                Text(stringResource(Res.string.assignment_title) + "*")
            },
            onValueChange = { newTitle ->
                assignment?.also {
                    onEntityChanged(it.copy(title = newTitle))
                }
            },
            supportingText = {
                Text(uiTextStringResource(uiState.nameError ?: Res.string.required.asUiText()))
            },
            isError = uiState.nameError != null,
        )

        var expanded by remember { mutableStateOf(false) }

        //As per https://developer.android.com/reference/kotlin/androidx/compose/material3/package-summary#ExposedDropdownMenuBox(kotlin.Boolean,kotlin.Function1,androidx.compose.ui.Modifier,kotlin.Function1)
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth()
                    .defaultItemPadding()
                    .testTag("class_dropdown_textfield")
                    .menuAnchor(MenuAnchorType.PrimaryEditable),
                value = uiState.assigneeText,
                label = {
                    Text(stringResource(Res.string.assign_to))
                },
                onValueChange = {
                    onAssigneeTextChanged(it)
                },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = expanded,
                        modifier = Modifier.menuAnchor(MenuAnchorType.SecondaryEditable),
                    )
                },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                supportingText = {
                    Text(uiTextStringResource(uiState.classError ?: Res.string.required.asUiText()))
                },
                isError = uiState.classError != null,
            )

            ExposedDropdownMenu(
                modifier = Modifier.heightIn(max = 280.dp),
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                filteredOptions.forEach { clazz ->
                    DropdownMenuItem(
                        text = {
                            Text(clazz.title)
                        },
                        onClick = {
                            expanded = false
                            onAssigneeClassSelected(clazz)
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    )
                }
            }
        }

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth().defaultItemPadding().testTag("description"),
            value = assignment?.description ?: "",
            label = {
                Text(stringResource(Res.string.description))
            },
            onValueChange = { newDescription ->
                assignment?.also {
                    onEntityChanged(it.copy(description = newDescription))
                }
            }
        )

        RespectLocalDateTimeField(
            modifier = Modifier.defaultItemPadding().fillMaxWidth(),
            value = assignment?.deadline?.toLocalDateTime(TimeZone.currentSystemDefault()),
            onValueChanged = { newDeadline ->
                assignment?.also {
                    onEntityChanged(
                        it.copy(
                            deadline = newDeadline?.toInstant(TimeZone.currentSystemDefault())
                        )
                    )
                }
            },
            dateTestTag = "due_date",
            timeTestTag = "due_time",
        )

        Text(
            modifier = Modifier.defaultItemPadding(),
            text = stringResource(Res.string.tasks),
            style = MaterialTheme.typography.titleMedium
        )

        OutlinedButton(
            onClick = { onClickAddLearningUnit() },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(1.dp, Color.LightGray)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Task", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.weight(1f))
        }
        Spacer(Modifier.height(16.dp))

        val currentTasks = assignment?.learningUnits ?: emptyList()

        if (currentTasks.isEmpty()) {
            EmptyTasksIllustration()
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                currentTasks.forEach { learningUnit ->
                    TaskListItem(
                        learningUnit = learningUnit,
                        uiState = uiState,
                        onRemove = { onClickRemoveLearningUnit(learningUnit) }
                    )
                }
            }
        }
    }
}

@Composable
fun TaskListItem(
    learningUnit: AssignmentLearningUnitRef,
    uiState: AssignmentEditUiState,
    onRemove: () -> Unit
) {
    val infoFlow = remember(learningUnit.learningUnitManifestUrl) {
        uiState.learningUnitInfoFlow(learningUnit.learningUnitManifestUrl)
    }
    val info by infoFlow.collectAsState(DataLoadingState())
    val data = info.dataOrNull()

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Menu,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(20.dp)
        )

        Spacer(Modifier.width(12.dp))

        val iconLink = data?.findIcons()?.firstOrNull()
        AsyncImage(
            model = iconLink?.let {
                learningUnit.learningUnitManifestUrl.resolve(it.href).toString()
            },
            contentDescription = null,
            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(4.dp))
        )

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = data?.metadata?.title?.getTitle() ?: "Loading...",
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = data?.metadata?.description ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        IconButton(onClick = onRemove) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Remove",
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun EmptyTasksIllustration() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(Res.drawable.fingerprint),
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = Color.Unspecified
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(Res.string.no_tasks_selected_yet),
            style = MaterialTheme.typography.bodyLarge,
            color = Color.DarkGray
        )
        Text(
            text = stringResource(Res.string.please_click_plus_button_to_add_one),
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}
