package world.respect.app.view.clazz.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.paging.compose.collectAsLazyPagingItems
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.RespectListSortHeader
import world.respect.app.components.RespectPersonAvatar
import world.respect.app.components.respectPagingItems
import world.respect.app.components.respectRememberPager
import world.respect.datalayer.school.model.EnrollmentRoleEnum
import world.respect.datalayer.school.model.Person
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.add_teacher
import world.respect.shared.generated.resources.add_student
import world.respect.shared.generated.resources.description
import world.respect.shared.generated.resources.pending_requests
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
    onClickAddPersonToClazz: (EnrollmentRoleEnum) -> Unit,
    onSortOrderChanged: (SortOrderOption) -> Unit = { },
    onSelectChip: (String) -> Unit,
    onClickAcceptInvite: (Person) -> Unit,
    onClickDismissInvite: (Person) -> Unit,
    onTogglePendingSection: () -> Unit,
    onToggleTeachersSection: () -> Unit,
    onToggleStudentsSection: () -> Unit
) {
    val teacherPager = respectRememberPager(uiState.teachers)
    val studentPager = respectRememberPager(uiState.students)

    val pendingTeacherPager = respectRememberPager(uiState.pendingTeachers)
    val pendingStudentPager = respectRememberPager(uiState.pendingStudents)

    val teacherLazyPagingItems = teacherPager.flow.collectAsLazyPagingItems()
    val studentLazyPagingItems = studentPager.flow.collectAsLazyPagingItems()
    val pendingTeacherLazyPagingItems = pendingTeacherPager.flow.collectAsLazyPagingItems()
    val pendingStudentLazyPagingItems = pendingStudentPager.flow.collectAsLazyPagingItems()

    fun Person?.key(role: EnrollmentRoleEnum, index: Int) : Any {
        return this?.guid?.let {
            Pair(it, role)
        } ?: "${role}_$index"
    }


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

//            RespectFilterChipsHeader(
//                options = uiState.chipOptions.map { it.option },
//                selectedOption = uiState.selectedChip,
//                onOptionSelected = { onSelectChip(it) },
//                optionLabel = { it }
//            )

            RespectListSortHeader(
                activeSortOrderOption = uiState.activeSortOrderOption,
                sortOptions = uiState.sortOptions,
                enabled = uiState.fieldsEnabled,
                onClickSortOption = onSortOrderChanged,
            )
        }

        if((uiState.showAddTeacher || uiState.showAddStudent) &&
            (pendingTeacherLazyPagingItems.itemCount + pendingStudentLazyPagingItems.itemCount) > 0
        ) {
            item("pending_header") {
                ListItem(
                    modifier = Modifier
                        .clickable { onTogglePendingSection() },
                    headlineContent = {
                        Text(
                            text = stringResource(
                                resource = Res.string.pending_requests
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
        }

        if (uiState.isPendingExpanded) {
            if(uiState.showAddTeacher) {
                respectPagingItems(
                    items = pendingTeacherLazyPagingItems,
                    key = { person, index ->
                        person.key(EnrollmentRoleEnum.PENDING_TEACHER, index)
                    }
                ) { person ->
                    ListItem(
                        modifier = Modifier.fillMaxWidth(),
                        leadingContent = {
                            RespectPersonAvatar(
                                name = person?.givenName ?: ""
                            )
                        },
                        headlineContent = {
                            Text(text = person?.givenName ?: "")
                        },
                        supportingContent = {
                            Text(person?.roles?.firstOrNull()?.roleEnum?.value ?: "")
                        },
                        trailingContent = {
                            Row {
                                Icon(
                                    modifier = Modifier.size(24.dp)
                                        .clickable {
                                            person?.also(onClickAcceptInvite)
                                        },
                                    imageVector = Icons.Outlined.CheckCircle,
                                    contentDescription = stringResource(resource = Res.string.accept_invite)
                                )

                                Spacer(Modifier.width(16.dp))

                                Icon(
                                    modifier = Modifier.size(24.dp).clickable {
                                        person?.also(onClickDismissInvite)
                                    },
                                    imageVector = Icons.Outlined.Cancel,
                                    contentDescription = stringResource(resource = Res.string.dismiss_invite)
                                )
                            }
                        }
                    )
                }
            }


            if(uiState.showAddStudent) {
                respectPagingItems(
                    items = pendingStudentLazyPagingItems,
                    key = { person, index ->
                        person.key(EnrollmentRoleEnum.PENDING_STUDENT, index)
                    }
                ) { person ->
                    ListItem(
                        modifier = Modifier
                            .fillMaxWidth(),
                        leadingContent = {
                            RespectPersonAvatar(
                                name = person?.givenName ?: ""
                            )
                        },
                        headlineContent = {
                            Text(text = person?.givenName ?: "")
                        },
                        supportingContent = {
                            Text(person?.roles?.firstOrNull()?.roleEnum?.value ?: "")
                        },
                        trailingContent = {
                            Row {
                                Icon(
                                    modifier = Modifier.size(24.dp)
                                        .clickable {
                                            person?.also(onClickAcceptInvite)
                                        },
                                    imageVector = Icons.Outlined.CheckCircle,
                                    contentDescription = stringResource(resource = Res.string.accept_invite)
                                )

                                Spacer(Modifier.width(16.dp))

                                Icon(
                                    modifier = Modifier.size(24.dp).clickable {
                                        person?.also(onClickDismissInvite)
                                    },
                                    imageVector = Icons.Outlined.Cancel,
                                    contentDescription = stringResource(resource = Res.string.dismiss_invite)
                                )
                            }
                        }
                    )
                }
            }
        }

        item("teacher_header") {
            ListItem(
                modifier = Modifier
                    .clickable { onToggleTeachersSection() },

                headlineContent = {
                    Text(
                        modifier = Modifier.padding(top = 24.dp),
                        text = stringResource(Res.string.teachers)
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

        if(uiState.isTeachersExpanded) {
            if(uiState.showAddTeacher) {
                item("add_teacher") {
                    ListItem(
                        modifier = Modifier.clickable {
                            onClickAddPersonToClazz(EnrollmentRoleEnum.TEACHER)
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
            }

            respectPagingItems(
                items = teacherLazyPagingItems,
                key = { person, index ->
                    person.key(EnrollmentRoleEnum.TEACHER, index)
                }
            ) { teacher ->
                ListItem(
                    modifier = Modifier
                        .fillMaxWidth(),
                    leadingContent = {
                        RespectPersonAvatar(
                            name = teacher?.givenName ?: ""
                        )
                    },
                    headlineContent = {
                        Text(text = teacher?.givenName ?: "")
                    }
                )
            }
        }

        item("student_header") {
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
            if(uiState.showAddStudent) {
                item("add_student") {
                    ListItem(
                        modifier = Modifier.clickable {
                            onClickAddPersonToClazz(EnrollmentRoleEnum.STUDENT)
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
            }

            respectPagingItems(
                items = studentLazyPagingItems,
                key = { person, index ->
                    person.key(EnrollmentRoleEnum.STUDENT, index)
                }
            ) { student ->
                ListItem(
                    modifier = Modifier
                        .fillMaxWidth(),

                    leadingContent = {
                        RespectPersonAvatar(
                            name = student?.givenName ?: ""
                        )
                    },

                    headlineContent = {
                        Text(
                            text = student?.givenName ?: ""
                        )
                    }
                )
            }
        }
    }
}

