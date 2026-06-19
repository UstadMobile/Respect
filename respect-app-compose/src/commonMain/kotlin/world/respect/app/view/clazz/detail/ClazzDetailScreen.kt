package world.respect.app.view.clazz.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.paging.compose.collectAsLazyPagingItems
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.RespectListSortHeader
import world.respect.app.components.RespectPersonAvatar
import world.respect.app.components.respectPagingItems
import world.respect.app.components.respectRememberPager
import world.respect.datalayer.db.school.ext.fullName
import world.respect.app.components.langMapString
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.datalayer.school.model.EnrollmentRoleEnum
import world.respect.datalayer.school.model.Person
import world.respect.lib.xapi.ext.objectActivityOrNull
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.add_student
import world.respect.shared.generated.resources.add_teacher
import world.respect.shared.generated.resources.description
import world.respect.shared.generated.resources.pending_requests
import world.respect.shared.generated.resources.collapse_pending_invites
import world.respect.shared.generated.resources.collapse_students
import world.respect.shared.generated.resources.collapse_teachers
import world.respect.shared.generated.resources.collapse_groups
import world.respect.shared.generated.resources.expand_pending_invites
import world.respect.shared.generated.resources.expand_students
import world.respect.shared.generated.resources.expand_teachers
import world.respect.shared.generated.resources.expand_groups
import world.respect.shared.generated.resources.manage_enrollments
import world.respect.shared.generated.resources.more_options
import world.respect.shared.generated.resources.remove_from_class
import world.respect.shared.generated.resources.student
import world.respect.shared.generated.resources.students
import world.respect.shared.generated.resources.teacher
import world.respect.shared.generated.resources.teachers
import world.respect.shared.util.SortOrderOption
import world.respect.shared.viewmodel.clazz.detail.ClazzDetailUiState
import world.respect.shared.viewmodel.clazz.detail.ClazzDetailViewModel
import world.respect.shared.generated.resources.create_group
import world.respect.shared.generated.resources.groups
import world.respect.shared.viewmodel.clazz.detail.ClazzDetailViewModel.Companion.STACK_COUNT

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
        onToggleStudentsSection = viewModel::onToggleStudentsSection,
        onToggleStudentGroupingSection = viewModel::onToggleStudentGroupingSection,
        onClickRemovePersonFromClass = viewModel::onClickRemovePersonFromClass,
        onClickManageEnrollments = viewModel::onClickManageEnrollments,
        onClickPerson = viewModel::onClickPerson,
        onClickCreateGroup=viewModel::onClickCreateGroup,
        onClickGroup = viewModel::onClickGroup
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
    onToggleStudentsSection: () -> Unit,
    onToggleStudentGroupingSection: () -> Unit,
    onClickRemovePersonFromClass: (Person, EnrollmentRoleEnum) -> Unit,
    onClickManageEnrollments: (Person, EnrollmentRoleEnum) -> Unit,
    onClickPerson: (Person) -> Unit,
    onClickCreateGroup:()-> Unit,
    onClickGroup: (String) -> Unit,
) {
    val teacherPager = respectRememberPager(uiState.teachers)
    val studentPager = respectRememberPager(uiState.students)

    val pendingTeacherPager = respectRememberPager(uiState.pendingTeachers)
    val pendingStudentPager = respectRememberPager(uiState.pendingStudents)

    val teacherLazyPagingItems = teacherPager.flow.collectAsLazyPagingItems()
    val studentLazyPagingItems = studentPager.flow.collectAsLazyPagingItems()
    val pendingTeacherLazyPagingItems = pendingTeacherPager.flow.collectAsLazyPagingItems()
    val pendingStudentLazyPagingItems = pendingStudentPager.flow.collectAsLazyPagingItems()

    val classStatement = uiState.classStatement.dataOrNull()

    fun Person?.key(role: EnrollmentRoleEnum, index: Int): Any {
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

                supportingContent = {
                    Text(
                        text =classStatement?.objectActivityOrNull()?.definition?.description?.let {
                                langMapString(it)
                            } ?: "",
                    )
                }
            )
        }

        item {
            RespectListSortHeader(
                activeSortOrderOption = uiState.activeSortOrderOption,
                sortOptions = uiState.sortOptions,
                enabled = uiState.fieldsEnabled,
                onClickSortOption = onSortOrderChanged,
            )
        }

        if (uiState.isAdminOrTeacher &&
            (pendingTeacherLazyPagingItems.itemCount + pendingStudentLazyPagingItems.itemCount) > 0
        ) {
            item("pending_header") {
                ListItem(
                    modifier = Modifier
                        .clickable { onTogglePendingSection() },
                    headlineContent = {
                        Text(
                            text = stringResource(Res.string.pending_requests)
                                    + " (${
                                pendingTeacherLazyPagingItems.itemCount
                                        + pendingStudentLazyPagingItems.itemCount
                            })"
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
            if (uiState.isAdminOrTeacher) {
                respectPagingItems(
                    items = pendingTeacherLazyPagingItems,
                    key = { person, index ->
                        person.key(EnrollmentRoleEnum.PENDING_TEACHER, index)
                    }
                ) { person ->
                    ClassPendingPersonListItem(
                        person = person,
                        pendingRole = Res.string.teacher,
                        showApproveDeclineOptions = person?.let {
                            uiState.showApproveOption(it)
                        } ?: false,
                        onClickAcceptInvite = onClickAcceptInvite,
                        onClickDismissInvite = onClickDismissInvite,
                    )
                }
            }

            if (uiState.isAdminOrTeacher) {
                respectPagingItems(
                    items = pendingStudentLazyPagingItems,
                    key = { person, index ->
                        person.key(EnrollmentRoleEnum.PENDING_STUDENT, index)
                    }
                ) { person ->
                    ClassPendingPersonListItem(
                        person = person,
                        pendingRole = Res.string.student,
                        showApproveDeclineOptions = person?.let {
                            uiState.showApproveOption(it)
                        } ?: false,
                        onClickAcceptInvite = onClickAcceptInvite,
                        onClickDismissInvite = onClickDismissInvite,
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
                        text = if (teacherLazyPagingItems.itemCount > 0)
                            "${stringResource(Res.string.teachers)} (${teacherLazyPagingItems.itemCount})"
                        else
                            stringResource(Res.string.teachers)
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
            if (uiState.isAdminOrTeacher) {
                item("add_teacher") {
                    ListItem(
                        modifier = Modifier.clickable {
                            onClickAddPersonToClazz(EnrollmentRoleEnum.TEACHER)
                        },
                        leadingContent = {
                            Icon(
                                modifier = Modifier.size(40.dp).padding(8.dp),
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
                key = { person, index -> person.key(EnrollmentRoleEnum.TEACHER, index) }
            ) { teacher ->
                PersonListItemWithMenu(
                    person = teacher,
                    showMenu = uiState.isAdminOrTeacher,
                    onClickRemove = { onClickRemovePersonFromClass(it, EnrollmentRoleEnum.TEACHER) },
                    onClickManage = { onClickManageEnrollments(it, EnrollmentRoleEnum.TEACHER) },
                    onClick = onClickPerson,
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
                        text = if (studentLazyPagingItems.itemCount > 0)
                            "${stringResource(Res.string.students)} (${studentLazyPagingItems.itemCount})"
                        else
                            stringResource(Res.string.students)
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
                            )
                    )
                }
            )
        }

        if (uiState.isStudentsExpanded) {
            if(uiState.isAdminOrTeacher) {
                item("add_student") {
                    ListItem(
                        modifier = Modifier.clickable {
                            onClickAddPersonToClazz(EnrollmentRoleEnum.STUDENT)
                        },

                        leadingContent = {
                            Icon(
                                modifier = Modifier.size(40.dp).padding(8.dp),
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
                key = { person, index -> person.key(EnrollmentRoleEnum.STUDENT, index) }
            ) { student ->
                PersonListItemWithMenu(
                    person = student,
                    onClickRemove = {
                        onClickRemovePersonFromClass(it, EnrollmentRoleEnum.STUDENT)
                    },
                    onClickManage = { onClickManageEnrollments(it, EnrollmentRoleEnum.STUDENT) },
                    showMenu = uiState.isAdminOrTeacher,
                    onClick = onClickPerson,
                )
            }
        }

        if (uiState.isAdminOrTeacher) {

            item("student_grouping_header") {
                ListItem(
                    modifier = Modifier
                        .clickable { onToggleStudentGroupingSection() },
                    headlineContent = {
                        Text(
                            modifier = Modifier.padding(top = 24.dp),
                            text = if (uiState.groups.isNotEmpty())
                                "${stringResource(Res.string.groups)} (${uiState.groups.size})"
                            else
                                stringResource(Res.string.groups)
                        )
                    },

                    trailingContent = {
                        Icon(
                            imageVector = Icons.Outlined.KeyboardArrowDown,
                            contentDescription = if (uiState.isStudentGroupingExpanded) {
                                stringResource(Res.string.collapse_groups)
                            } else {
                                stringResource(Res.string.expand_groups)
                            },
                            modifier = Modifier.size(24.dp)
                                .rotate(
                                    if (uiState.isStudentGroupingExpanded) 0f else -90f
                                )
                        )
                    }
                )
            }

            if (uiState.isStudentGroupingExpanded) {
                item("student_grouping") {
                    ListItem(
                        modifier = Modifier.clickable {
                            onClickCreateGroup()
                        },
                        leadingContent = {
                            Icon(
                                modifier = Modifier.size(40.dp).padding(8.dp),
                                imageVector = Icons.Filled.Add,
                                contentDescription = stringResource(resource = Res.string.create_group)
                            )
                        },
                        headlineContent = {
                            Text(
                                text =
                                    stringResource(resource = Res.string.create_group)
                            )
                        }
                    )
                }

                items(
                    items = uiState.groups,
                    key = { group -> group.account?.name ?: "" }
                ) { group ->
                    val groupId = group.account?.name ?: return@items

                    val memberNames = group.member
                        ?.map { it.name.orEmpty() }
                        ?: emptyList()

                    ListItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onClickGroup(groupId)
                            },
                        leadingContent = {
                            Box(
                                modifier = Modifier.width(40.dp),
                            ) {
                                val displayMembers = memberNames.take(STACK_COUNT)
                                displayMembers.forEachIndexed { i, name ->
                                    Box(
                                        modifier = Modifier
                                            .offset(x = (i * 12).dp)
                                            .zIndex((displayMembers.size - i).toFloat())
                                    ) {
                                        RespectPersonAvatar(
                                            name = name,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                        },
                        headlineContent = {
                            Text(text = "${group.name} (${memberNames.size})")
                        }
                    )
                }
            }
        }

    }
}

@Composable
fun PersonListItemWithMenu(
    person: Person?,
    showMenu: Boolean = false,
    onClickRemove: (Person) -> Unit,
    onClickManage: (Person) -> Unit,
    onClick: (Person) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    ListItem(
        modifier = Modifier.fillMaxWidth().clickable {
            person?.also(onClick)
        },
        leadingContent = {
            RespectPersonAvatar(name = person?.fullName() ?: "")
        },
        headlineContent = {
            Text(text = person?.fullName().orEmpty())
        },
        trailingContent = if(showMenu) {
            {
                IconButton(onClick = { expanded = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = stringResource(resource = Res.string.more_options)
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(Res.string.remove_from_class)) },
                        onClick = {
                            expanded = false
                            person?.also(onClickRemove)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(Res.string.manage_enrollments)) },
                        onClick = {
                            expanded = false
                            person?.also(onClickManage)
                        }
                    )
                }
            }
        }else {
            null
        }
    )
}
