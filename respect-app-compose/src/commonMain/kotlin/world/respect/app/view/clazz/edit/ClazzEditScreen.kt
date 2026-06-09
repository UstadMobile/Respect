package world.respect.app.view.clazz.edit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import kotlinx.coroutines.Dispatchers
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.LangMapTextField
import world.respect.app.components.defaultItemPadding
import world.respect.app.components.uiTextStringResource
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.lib.xapi.ext.copyWithObjectActivityDescription
import world.respect.lib.xapi.ext.copyWithObjectActivityName
import world.respect.lib.xapi.ext.objectActivityOrNull
import world.respect.lib.xapi.model.XapiStatement
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.class_name
import world.respect.shared.generated.resources.description
import world.respect.shared.generated.resources.required
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.clazz.edit.ClazzEditUiState
import world.respect.shared.viewmodel.clazz.edit.ClazzEditViewModel

@Composable
fun ClazzEditScreen(
    viewModel: ClazzEditViewModel
) {
    val uiState by viewModel.uiState.collectAsState(Dispatchers.Main.immediate)
    ClazzEditScreen(
        uiState = uiState,
        onEntityChanged = viewModel::onEntityChanged,
    )
}

@Composable
fun ClazzEditScreen(
    uiState: ClazzEditUiState,
    onEntityChanged: (XapiStatement) -> Unit = {},
) {

    val statement = uiState.statementData.dataOrNull()
    val fieldsEnabled = uiState.fieldsEnabled

    Column(
        modifier = Modifier.fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        LangMapTextField(
            modifier = Modifier.fillMaxWidth().defaultItemPadding().testTag("name"),
            value = statement?.objectActivityOrNull()?.definition?.name ?: emptyMap(),
            onValueChange = { value ->
                statement?.copyWithObjectActivityName(value)?.also { onEntityChanged(it) }
            },
            label = {
                Text(stringResource(Res.string.class_name) + "*")
            },
            supportingText = {
                Text(uiTextStringResource(uiState.clazzNameError ?: Res.string.required.asUiText()))
            },
            enabled = fieldsEnabled,
        )

        LangMapTextField(
            modifier = Modifier.fillMaxWidth().defaultItemPadding().testTag("description"),
            value = statement?.objectActivityOrNull()?.definition?.description ?: emptyMap(),
            label = {
                Text(stringResource(Res.string.description))
            },
            onValueChange = { value ->
                statement?.copyWithObjectActivityDescription(value)?.also { onEntityChanged(it) }
            },
            enabled = fieldsEnabled,
        )
    }
}

