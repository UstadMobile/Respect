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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.RespectFilterChipsHeader
import world.respect.app.components.RespectListSortHeader
import world.respect.app.components.RespectPersonAvatar
import world.respect.datalayer.oneroster.model.OneRosterRoleEnum
import world.respect.datalayer.school.model.Person
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.add_teacher
import world.respect.shared.generated.resources.add_student
import world.respect.shared.generated.resources.description
import world.respect.shared.generated.resources.pending_invite
import world.respect.shared.generated.resources.accept_invite
import world.respect.shared.generated.resources.collapse_pending_invites
import world.respect.shared.generated.resources.collapse_students
import world.respect.shared.generated.resources.collapse_teachers
import world.respect.shared.generated.resources.dismiss_invite
import world.respect.shared.generated.resources.expand_pending_invites
import world.respect.shared.generated.resources.expand_students
import world.respect.shared.generated.resources.expand_teachers
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
        onSelectChip = viewModel::onSelectChip,
        onClickAcceptInvite = viewModel::onClickAcceptInvite,
        onClickDismissInvite = viewModel::onClickDismissInvite,
        onTogglePendingSection = viewModel::onTogglePendingSection,
        onToggleTeachersSection = viewModel::onToggleTeachersSection,
        onToggleStudentsSection = viewModel::onToggleStudentsSection

    )
}

@Composable
fun ClazzDetailScreen(
    uiState: ClazzDetailUiState,
    onClickAddPersonToClazz: (OneRosterRoleEnum) -> Unit,
    onSortOrderChanged: (SortOrderOption) -> Unit = { },
    onSelectChip: (String) -> Unit,
    onClickAcceptInvite: (Person) -> Unit,
    onClickDismissInvite: (Person) -> Unit,
    onTogglePendingSection: () -> Unit,
    onToggleTeachersSection: () -> Unit,
    onToggleStudentsSection: () -> Unit

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

                /**Description field needed**/

                /* supportingContent = {
                     Text(
                        uiState.clazzDetail?.description
                     )
                 }*/
            )
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
                modifier = Modifier
                    .clickable { onTogglePendingSection() },
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
                        contentDescription =
                            if (uiState.isPendingExpanded) {
                                stringResource(
                                    Res.string.collapse_pending_invites
                                )
                            } else {
                                stringResource(
                                    Res.string.expand_pending_invites
                                )
                            },
                        modifier = Modifier.size(24.dp)
                            .rotate(
                                if (uiState.isPendingExpanded) 0f else -90f
                            ),
                    )
                }
            )
        }

        if (uiState.isPendingExpanded) {
            itemsIndexed(
                items = uiState.listOfPending,
                key = { index, pendingUser ->
                    pendingUser.guid
                }
            ) { index, user ->

                val roleName = user.roles.firstOrNull()?.roleType?.name
                    ?.lowercase()
                    ?.replaceFirstChar { it.uppercase() } ?: ""

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
                                modifier = Modifier.size(24.dp)
                                    .clickable {
                                        onClickAcceptInvite(user)
                                    },
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = stringResource(resource = Res.string.accept_invite)
                            )
                            Icon(
                                modifier = Modifier.size(24.dp).clickable {
                                    onClickDismissInvite(user)
                                },
                                imageVector = Icons.Outlined.Cancel,
                                contentDescription = stringResource(resource = Res.string.dismiss_invite)
                            )
                        }
                    }
                )
            }
        }

        item {
            ListItem(
                modifier = Modifier
                    .clickable { onToggleTeachersSection() },

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
                        contentDescription = if (uiState.isTeachersExpanded) {
                            stringResource(Res.string.collapse_teachers)
                        } else {
                            stringResource(Res.string.expand_teachers)
                        },
                        modifier = Modifier.size(24.dp)
                            .rotate(
                                if (uiState.isTeachersExpanded) 0f else -90f
                            ),
                    )
                }
            )
        }
        if (uiState.isTeachersExpanded) {

            item {
                ListItem(
                    modifier = Modifier.clickable {
                        onClickAddPersonToClazz(OneRosterRoleEnum.TEACHER)
                    },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = stringResource(resource = Res.string.add_teacher)
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
                items = uiState.listOfTeachers,
                key = { index, teacher ->
                    teacher.guid
                }
            ) { index, teacher ->
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
        }

        item {
            ListItem(
                modifier = Modifier
                    .clickable { onToggleStudentsSection() },

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
                        contentDescription = if (uiState.isStudentsExpanded) {
                            stringResource(Res.string.collapse_students)
                        } else {
                            stringResource(Res.string.expand_students)
                        },
                        modifier = Modifier.size(24.dp)
                            .rotate(
                                if (uiState.isStudentsExpanded) 0f else -90f
                            ),
                    )
                }
            )
        }

        if (uiState.isStudentsExpanded) {

            item {
                ListItem(
                    modifier = Modifier.clickable {
                        onClickAddPersonToClazz(OneRosterRoleEnum.STUDENT)
                    },

                    leadingContent = {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = stringResource(resource = Res.string.add_student)
                        )
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
                items = uiState.listOfStudents,
                key = { index, student ->
                    student.guid
                }
            ) { index, student ->
                ListItem(
                    modifier = Modifier
                        .fillMaxWidth(),

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
}

