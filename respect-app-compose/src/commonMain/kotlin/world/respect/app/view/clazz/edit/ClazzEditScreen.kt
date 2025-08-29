package world.respect.app.view.clazz.edit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import world.respect.shared.viewmodel.clazz.edit.ClazzEditViewModel
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.defaultItemPadding
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.class_name_label
import world.respect.shared.viewmodel.clazz.edit.ClazzEditUiState
import world.respect.datalayer.ext.dataOrNull
import world.respect.datalayer.oneroster.model.OneRosterClass
import world.respect.shared.generated.resources.required

@Composable
fun ClazzEditScreen(
    viewModel: ClazzEditViewModel
) {
    val uiState by viewModel.uiState.collectAsState(Dispatchers.Main.immediate)
    ClazzEditScreen(
        uiState = uiState,
        onEntityChanged = viewModel::onEntityChanged,
        onClearError = { viewModel.onClearError() }
    )
}

@Composable
fun ClazzEditScreen(
    uiState: ClazzEditUiState,
    onEntityChanged: (OneRosterClass) -> Unit = {},
    onClearError: () -> Unit = {},
) {

    val clazz = uiState.clazz.dataOrNull()
    val fieldsEnabled = uiState.fieldsEnabled

    Column(
        modifier = Modifier.fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth().defaultItemPadding(top = 16.dp),
            value = clazz?.title ?: "",
            label = {
                Text(
                    stringResource(Res.string.class_name_label)
                )
            },
            onValueChange = { value ->
                clazz?.also {
                    onEntityChanged(it.copy(title = value))
                }
                if (uiState.clazzNameError != null && value.isNotBlank()) {
                    onClearError()
                }
            },
            singleLine = true,
            supportingText = {
                Text(stringResource(Res.string.required))
            },
            enabled=fieldsEnabled,
            isError = uiState.clazzNameError != null
        )
        /**Description field needed**/

        /*
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth().editScreenPadding(),
                    value = uiState.entity?.description ?: "",
                    label = {
                        Text(
                            stringResource(Res.string.description)
                        )
                    },
                    singleLine = true,
                    onValueChange =
                        { newValue ->
                        uiState.entity?.let { current ->
                            onClazzChanged(
                                current.copy(description = newValue)
                            )
                        }
                    }
                )
        */
    }
}

