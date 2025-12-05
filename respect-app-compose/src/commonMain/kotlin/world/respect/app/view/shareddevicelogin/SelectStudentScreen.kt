package world.respect.app.view.shareddevicelogin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.paging.compose.collectAsLazyPagingItems
import world.respect.app.components.RespectPersonAvatar
import world.respect.app.components.respectPagingItems
import world.respect.app.components.respectRememberPager
import world.respect.datalayer.school.model.EnrollmentRoleEnum
import world.respect.datalayer.school.model.Person
import world.respect.shared.util.ext.fullName
import world.respect.shared.viewmodel.sharedschooldevicelogin.SelectStudentUiState
import world.respect.shared.viewmodel.sharedschooldevicelogin.SharedSchoolDeviceLoginSelectStudentViewModel

@Composable
fun SelectStudentScreen(
    viewModel: SharedSchoolDeviceLoginSelectStudentViewModel
) {
    val uiState: SelectStudentUiState by viewModel.uiState.collectAsState(
        SelectStudentUiState()
    )

    SelectStudentScreen(
        uiState = uiState,
        onClickStudent = viewModel::onClickStudent
    )
}

@Composable
fun SelectStudentScreen(
    uiState: SelectStudentUiState,
    onClickStudent: (Person) -> Unit,
    ) {
    val studentPager = respectRememberPager(uiState.students)
    val studentLazyPagingItems = studentPager.flow.collectAsLazyPagingItems()
    fun Person?.key(role: EnrollmentRoleEnum, index: Int): Any {
        return this?.guid?.let {
            Pair(it, role)
        } ?: "${role}_$index"
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        respectPagingItems(
            items = studentLazyPagingItems,
            key = { person, index -> person.key(EnrollmentRoleEnum.STUDENT, index) }
        ) { student ->
            ListItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        student?.also(onClickStudent)
                    },

                leadingContent = {
                    RespectPersonAvatar(name = student?.fullName() ?: "")
                },

                headlineContent = {
                    Text(text = student?.fullName().orEmpty())
                }
            )
        }

    }
}