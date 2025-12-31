package world.respect.app.view.assignment.edit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LibraryAdd
import androidx.compose.material3.ListItem
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
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
import world.respect.shared.generated.resources.add_from_playlist
import world.respect.shared.generated.resources.assignment_tasks
import world.respect.shared.generated.resources.clazz
import world.respect.shared.generated.resources.delete
import world.respect.shared.generated.resources.description
import world.respect.shared.generated.resources.lesson_assessment
import world.respect.shared.generated.resources.name
import world.respect.shared.generated.resources.required
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
        onClickAddFromPlaylists = viewModel::onClickAddFromPlaylists,
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
    onClickAddFromPlaylists: () -> Unit,
) {
    val assignment = uiState.assignment.dataOrNull()
    val filteredOptions = if(uiState.assigneeText.isNotBlank()) {
        uiState.classOptions.filter {
            it.title.contains(uiState.assigneeText, ignoreCase = true)
        }
    }else {
        uiState.classOptions
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth().defaultItemPadding().testTag("title"),
            value = assignment?.title ?: "",
            label = {
                Text(stringResource(Res.string.name) + "*")
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

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange =  { expanded = it }
        ) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth()
                    .defaultItemPadding()
                    .testTag("class_dropdown_textfield")
                    .menuAnchor(MenuAnchorType.PrimaryEditable),
                value = uiState.assigneeText,
                label = {
                    Text(stringResource(Res.string.clazz))
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
            text = stringResource(Res.string.assignment_tasks),
            style = MaterialTheme.typography.titleMedium
        )
        ListItem(
            modifier = Modifier.fillMaxWidth().clickable {
                onClickAddLearningUnit()
            },
            leadingContent = {
                Icon(
                    imageVector = Icons.Default.Add,
                    modifier = Modifier.size(40.dp).padding(8.dp),
                    contentDescription = ""
                )
            },
            headlineContent = {
                Text(stringResource(Res.string.lesson_assessment))
            }
        )

        if (uiState.showPlaylistButton) {
            ListItem(
                modifier = Modifier.fillMaxWidth().clickable {
                    onClickAddFromPlaylists()
                },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.LibraryAdd,
                        modifier = Modifier.size(40.dp).padding(8.dp),
                        contentDescription = "",
                    )
                },
                headlineContent = {
                    Text(
                        text = stringResource(Res.string.add_from_playlist),
                    )
                }
            )
        }
        assignment?.learningUnits?.forEach { learningUnit ->
            val learningUnitInfoFlow = remember(
                uiState.learningUnitInfoFlow, learningUnit.learningUnitManifestUrl
            ) {
                uiState.learningUnitInfoFlow(learningUnit.learningUnitManifestUrl)
            }

            val learningUnitInfo by learningUnitInfoFlow.collectAsState(DataLoadingState())

            ListItem(
                headlineContent = {
                    Text(learningUnitInfo.dataOrNull()?.metadata?.title?.getTitle() ?: "")
                },
                leadingContent = {
                    val iconLink = learningUnitInfo.dataOrNull()?.findIcons()?.firstOrNull()
                    val iconUrl = iconLink?.let {
                        learningUnit.learningUnitManifestUrl.resolve(it.href)
                    }
                    iconUrl?.also {
                        AsyncImage(
                            modifier = Modifier.size(40.dp),
                            model = iconUrl.toString(),
                            contentDescription = iconLink.title,
                        )
                    }
                },
                trailingContent = {
                    IconButton(
                        onClick = {
                            onClickRemoveLearningUnit(learningUnit)
                        }
                    )  {
                        Icon(
                            Icons.Default.Delete,
                            stringResource(Res.string.delete)
                        )
                    }
                },
            )
        }
    }
}
