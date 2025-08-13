package world.respect.app.view.clazz.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import world.respect.app.components.RespectFilterChipsHeader
import world.respect.app.components.RespectListSortHeader
import world.respect.app.components.RespectPersonAvatar
import world.respect.datalayer.oneroster.rostering.model.OneRosterUser
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.add_teacher
import world.respect.shared.generated.resources.add_student
import world.respect.shared.generated.resources.description
import world.respect.shared.generated.resources.end_date_label
import world.respect.shared.generated.resources.pending_invite
import world.respect.shared.generated.resources.start_date_label
import world.respect.shared.generated.resources.students
import world.respect.shared.generated.resources.teachers
import world.respect.shared.util.SortOrderOption
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
        onSortOrderChanged = viewModel::onSortOrderChanged,
        onSelectChip = { viewModel.onSelectChip(it) },
        onClickAcceptInvite = { viewModel.onClickAcceptInvite(it) },
        onClickDismissInvite = { viewModel.onClickDismissInvite(it) }
    )
}

@Composable
fun ClazzDetailScreen(
    uiState: ClazzDetailUiState,
    onClickAddPersonToClazz: () -> Unit,
    onSortOrderChanged: (SortOrderOption) -> Unit = { },
    onSelectChip: (String) -> Unit,
    onClickAcceptInvite: (OneRosterUser) -> Unit,
    onClickDismissInvite: (OneRosterUser) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {
        item {
            ListItem(
                headlineContent = {
                    Text(
                        text = stringResource(
                            resource = Res.string.description
                        )
                    )
                },
                supportingContent = {
                    Text(
                        text = stringResource(
                            resource = Res.string.description
                        )
                    )
                }
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ListItem(
                    headlineContent = {
                        Text(
                            text = stringResource(Res.string.start_date_label),
                        )
                    },
                    supportingContent = {
                        Text(text = "21/07/2025") // your dynamic date value here
                    },
                    modifier = Modifier.weight(1f)
                )

                ListItem(
                    headlineContent = {
                        Text(
                            text = stringResource(Res.string.end_date_label),
                        )
                    },
                    supportingContent = {
                        Text(text = "21/07/2026") // your dynamic date value here
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            RespectFilterChipsHeader(
                options = uiState.chipOptions.map { it.option },
                selectedOption = uiState.selectedChip,
                onOptionSelected = { onSelectChip(it) },
                optionLabel = { it }
            )

            RespectListSortHeader(
                activeSortOrderOption = uiState.activeSortOrderOption,
                sortOptions = uiState.sortOptions,
                enabled = uiState.fieldsEnabled,
                onClickSortOption = onSortOrderChanged,
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
            }) { index, user ->
            val roleName = user.roles.firstOrNull()?.role?.name
                ?.lowercase()
                ?.replaceFirstChar { it.uppercase() } ?: "Unknown"

            ListItem(
                modifier = Modifier
                    .fillMaxWidth(),
                leadingContent = {
                    RespectPersonAvatar(
                        name = user.givenName
                    )
                },
                headlineContent = {
                    Text(text = user.givenName + "(" + roleName + ")")
                },
                trailingContent = {
                    Row {
                        Icon(
                            modifier = Modifier.size(24.dp).clickable {
                                onClickAcceptInvite(user)
                            },
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                        )
                        Icon(
                            modifier = Modifier.size(24.dp).clickable {
                                onClickDismissInvite(user)
                            },
                            imageVector = Icons.Outlined.Cancel,
                            contentDescription = null,
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
                        name = teacher.givenName
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
                        name = student.givenName
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

