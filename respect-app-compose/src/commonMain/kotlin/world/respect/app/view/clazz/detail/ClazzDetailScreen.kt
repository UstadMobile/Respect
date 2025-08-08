package world.respect.app.view.clazz.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.RespectSortHeader
import world.respect.app.components.RespectFilterChipsHeader
import world.respect.app.components.RespectPersonAvatar
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.add_teacher
import world.respect.shared.generated.resources.add_student
import world.respect.shared.generated.resources.pending_invite
import world.respect.shared.generated.resources.students
import world.respect.shared.generated.resources.teachers
import world.respect.shared.viewmodel.clazz.detail.ClazzDetailUiState
import world.respect.shared.viewmodel.clazz.detail.ClazzDetailViewModel

@Composable
fun ClazzDetailScreen(
    viewModel: ClazzDetailViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    ClazzDetailScreen(
        uiState = uiState,
        onClickAddPersonToClazz = viewModel::onClickAddPersonToClazz,
        onClickSortOption = { viewModel.onClickSortOption(it) },
        onSelectChip = { viewModel.onSelectChip(it) },
    )
}

@Composable
fun ClazzDetailScreen(
    uiState: ClazzDetailUiState,
    onClickAddPersonToClazz: () -> Unit,
    onClickSortOption: (String) -> Unit,
    onSelectChip: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {
        item {

            RespectFilterChipsHeader(
                options = uiState.chipOptions.map{it.option},
                selectedOption = uiState.selectedChip,
                onOptionSelected = { onSelectChip(it) },
                optionLabel = { it }
            )

            RespectSortHeader(
                options = uiState.sortOptions.map { it.option },
                selectedOption = uiState.selectedSortOption,
                onOptionSelected = onClickSortOption,
                optionLabel = { it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp)
            )
        }

        item {
            ListItem(
                headlineContent = {
                    Text(
                        text = stringResource(
                            resource = Res.string.pending_invite
                        )
                    )
                },
                trailingContent = {
                    Icon(
                        imageVector = Icons.Outlined.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                    )
                }
            )
        }

        itemsIndexed(
            uiState.listOfPending,
            key = { index, pendingUser ->
                pendingUser.sourcedId
            }) { index, pendingUser ->
            val roleName = pendingUser.roles.firstOrNull()?.role?.name
                ?.lowercase()
                ?.replaceFirstChar { it.uppercase() } ?: "Unknown"

            ListItem(
                modifier = Modifier
                    .fillMaxWidth(),
                leadingContent = {
                    RespectPersonAvatar(
                        name = pendingUser.givenName,
                        modifier = Modifier.size(40.dp),
                        fontScale = 1.0f
                    )
                },
                headlineContent = {
                    Text(text = pendingUser.givenName + "(" + roleName + ")")
                },
                trailingContent = {
                    Row {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                        )
                        Icon(
                            imageVector = Icons.Outlined.Cancel,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
            )
        }


        item {
            ListItem(
                headlineContent = {
                    Text(
                        modifier = Modifier.padding(top = 24.dp),
                        text = stringResource(
                            resource = Res.string.teachers
                        )
                    )
                },
                trailingContent = {
                    Icon(
                        imageVector = Icons.Outlined.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                    )
                }
            )
        }

        item {
            ListItem(
                modifier = Modifier.clickable {
                    onClickAddPersonToClazz()
                },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null
                    )
                },
                headlineContent = {
                    Text(
                        text =
                            stringResource(resource = Res.string.add_teacher)
                    )
                }
            )
        }


        itemsIndexed(
            uiState.listOfTeachers,
            key = { index, teacher ->
                teacher.sourcedId
            }) { index, teacher ->
            ListItem(
                modifier = Modifier
                    .fillMaxWidth(),
                leadingContent = {
                    RespectPersonAvatar(
                        name = teacher.givenName,
                        modifier = Modifier.size(40.dp),
                        fontScale = 1.0f
                    )
                },
                headlineContent = {
                    Text(text = teacher.givenName)
                }
            )
        }

        item {
            ListItem(
                headlineContent = {
                    Text(
                        modifier = Modifier.padding(top = 24.dp),
                        text = stringResource(
                            resource = Res.string.students
                        )
                    )
                },
                trailingContent = {
                    Icon(
                        imageVector = Icons.Outlined.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                    )
                }
            )
        }

        item {
            ListItem(
                modifier = Modifier.clickable {
                    onClickAddPersonToClazz()
                },
                leadingContent = {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = null)
                },
                headlineContent = {
                    Text(
                        text =
                            stringResource(resource = Res.string.add_student)
                    )
                }
            )
        }

        itemsIndexed(
            uiState.listOfStudents,
            key = { index, student ->
                student.sourcedId
            }) { index, student ->
            ListItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { },
                leadingContent = {
                    RespectPersonAvatar(
                        name = student.givenName,
                        modifier = Modifier.size(40.dp),
                        fontScale = 1.0f
                    )
                },
                headlineContent = {
                    Text(
                        text = student.givenName
                    )
                }
            )
        }
    }
}

