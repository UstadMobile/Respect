package world.respect.app.view.schooldirectory.edit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.Dispatchers
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.defaultItemPadding
import world.respect.datalayer.ext.dataOrNull
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.enter_link
import world.respect.shared.generated.resources.required
import world.respect.shared.viewmodel.schooldirectory.edit.SchoolDirectoryEditUIState
import world.respect.shared.viewmodel.schooldirectory.edit.SchoolDirectoryEditViewModel


@Composable
fun SchoolDirectoryEditScreen(
    viewModel: SchoolDirectoryEditViewModel
) {
    val uiState by viewModel.uiState.collectAsState(Dispatchers.Main.immediate)
    SchoolDirectoryEditScreen(
        uiState = uiState,
        onClearError =  viewModel::onClearError

    )
}

@Composable
fun SchoolDirectoryEditScreen(
    uiState: SchoolDirectoryEditUIState,
    onClearError: () -> Unit = {},
    ) {
    val schoolDirectory = uiState.schoolDirectory.dataOrNull()
    val fieldsEnabled = uiState.fieldsEnabled

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth().defaultItemPadding(),
            value =  "",
            label = {
                Text(
                    stringResource(Res.string.enter_link)
                )
            },
            onValueChange = { value ->
             /*   schoolDirectory?.also {
                    onEntityChanged(it.copy(title = value))
                }*/
                if (uiState.schoolDirectoryUrlError != null && value.isNotBlank()) {
                    onClearError()
                }
            },
            singleLine = true,
            supportingText = {
                Text(stringResource(Res.string.required))
            },
            enabled = fieldsEnabled,
            isError = uiState.schoolDirectoryUrlError != null
        )
    }

}