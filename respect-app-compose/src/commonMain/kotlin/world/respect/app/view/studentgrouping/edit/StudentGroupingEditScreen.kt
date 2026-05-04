package world.respect.app.view.studentgrouping.edit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.paging.compose.collectAsLazyPagingItems
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.RespectPersonAvatar
import world.respect.app.components.defaultItemPadding
import world.respect.app.components.respectPagingItems
import world.respect.app.components.respectRememberPager
import world.respect.app.components.uiTextStringResource
import world.respect.datalayer.db.school.ext.fullName
import world.respect.datalayer.school.model.EnrollmentRoleEnum
import world.respect.datalayer.school.model.Person
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.name
import world.respect.shared.generated.resources.required
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.studentgrouping.edit.StudentGroupingEditUiState
import world.respect.shared.viewmodel.studentgrouping.edit.StudentGroupingEditViewModel


@Composable
fun StudentGroupingEditScreen(
    viewModel: StudentGroupingEditViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    StudentGroupingEditScreen(
        uiState = uiState,
        onStudentCheckedChange = viewModel::onStudentCheckedChange,
        onGroupNameChanged = viewModel::onGroupNameChanged
    )
}

@Composable
fun StudentGroupingEditScreen(
    uiState: StudentGroupingEditUiState,
    onStudentCheckedChange: (Person, Boolean) -> Unit,
    onGroupNameChanged: (String) -> Unit,

    ) {

    val studentPager = respectRememberPager(uiState.students)
    val studentLazyPagingItems = studentPager.flow.collectAsLazyPagingItems()

    fun Person?.key(role: EnrollmentRoleEnum, index: Int): Any {
        return this?.guid?.let {
            Pair(it, role)
        } ?: "${role}_$index"
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {

        item {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth()
                    .defaultItemPadding()
                    .testTag("name"),
                value = uiState.groupName,
                onValueChange = { value ->
                    onGroupNameChanged(value)
                },
                isError = uiState.groupNameError != null,
                singleLine = true,
                label = {
                    Text(stringResource(Res.string.name) + "*")
                },
                supportingText = {
                    Text(
                        uiTextStringResource(
                            uiState.groupNameError ?: Res.string.required.asUiText()
                        )
                    )
                }
            )
        }

        respectPagingItems(
            items = studentLazyPagingItems,
            key = { person, index ->
                person?.guid ?: "student_$index"
            }
        ) { student ->

            val isSelected = student?.guid in uiState.selectedStudentIds

            PersonListItemWithMenu(
                person = student,
                isSelected = isSelected,
                onCheckedChange = { checked ->
                    student?.let {
                        onStudentCheckedChange(it, checked)
                    }
                }
            )
        }
    }
}

@Composable
fun PersonListItemWithMenu(
    person: Person?,
    isSelected: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onCheckedChange(!isSelected)
            },
        leadingContent = {
            RespectPersonAvatar(name = person?.fullName() ?: "")
        },
        headlineContent = {
            Text(text = person?.fullName().orEmpty())
        },
        trailingContent = {
            Checkbox(
                checked = isSelected,
                onCheckedChange = onCheckedChange
            )
        }
    )
}