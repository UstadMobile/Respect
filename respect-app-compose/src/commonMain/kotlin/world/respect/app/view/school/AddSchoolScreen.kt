package world.respect.app.view.school

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import world.respect.app.components.LaunchCustomTab
import world.respect.app.components.defaultScreenPadding
import world.respect.shared.viewmodel.school.AddSchoolViewModel

@Composable
fun AddSchoolScreen(
    viewModel: AddSchoolViewModel

) {
    val uiState by viewModel.uiState.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .defaultScreenPadding()
    ) {
        LaunchCustomTab(url = uiState.url)
    }
}
