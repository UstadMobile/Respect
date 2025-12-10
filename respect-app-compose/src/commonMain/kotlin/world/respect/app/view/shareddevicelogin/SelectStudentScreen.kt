package world.respect.app.view.shareddevicelogin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.compose.collectAsLazyPagingItems
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

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize().padding(12.dp)
    ) {
        respectPagingItems(
            items = studentLazyPagingItems,
            key = { person, index -> person?.guid ?: "student_$index" }
        ) { student ->
            if (student != null) {
                StudentCardWithAvatar(
                    student = student,
                    onClick = { onClickStudent(student) }
                )
            }
        }

    }
}

@Composable
fun StudentCardWithAvatar(
    student: Person,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = student.fullName().take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = student.fullName(),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}