package world.respect.app.view.enrollment.edit

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import world.respect.datalayer.ext.dataOrNull
import world.respect.datalayer.school.model.Enrollment
import world.respect.datalayer.school.model.Person
import world.respect.shared.viewmodel.enrollment.edit.EnrollmentEditUiState
import world.respect.shared.viewmodel.enrollment.edit.EnrollmentEditViewModel

@Composable
fun EnrollmentEditScreen (
    viewModel: EnrollmentEditViewModel
){
    val uiState by viewModel.uiState.collectAsState()
    EnrollmentEditScreen(
        uiState = uiState,
        onEntityChanged = viewModel::onEntityChanged
    )
}

@Composable
fun EnrollmentEditScreen (
    uiState: EnrollmentEditUiState,
    onEntityChanged: (Enrollment) -> Unit,

    ) {

    val enrollment = uiState.enrollment.dataOrNull()

    Column {
        Text("Hello ${enrollment?.role}")

    }

}