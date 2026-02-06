package world.respect.app.view.sharedschooldevice

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.defaultItemPadding
import world.respect.app.components.uiTextStringResource
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.enter_school_device_pin
import world.respect.shared.generated.resources.next
import world.respect.shared.generated.resources.other_options
import world.respect.shared.viewmodel.sharedschooldevice.TeacherAndAdminLoginUiState
import world.respect.shared.viewmodel.sharedschooldevice.TeacherAndAdminLoginViewmodel


@Composable
fun TeacherAndAdminLoginScreen(
    viewModel: TeacherAndAdminLoginViewmodel,
) {
    val uiState by viewModel.uiState.collectAsState(context = Dispatchers.Main.immediate)
    TeacherAndAdminLoginScreen(
        uiState = uiState,
        onPinChanged = viewModel::onPinChanged,
        onClickNext = viewModel::onClickNext
    )
}
@Composable
fun TeacherAndAdminLoginScreen(
    uiState: TeacherAndAdminLoginUiState,
    onPinChanged: (String) -> Unit,
    onClickNext:() -> Unit
    ) {
    val focusRequester = remember { FocusRequester() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .defaultItemPadding(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TextField(
            value = uiState.pin,
            onValueChange = onPinChanged,
            label = {
                Text(text = stringResource(Res.string.enter_school_device_pin))
            },
            placeholder = {
                Text(text = stringResource(Res.string.enter_school_device_pin))
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            modifier = Modifier.testTag("Enter school device PIN")
                .fillMaxWidth()
                .background(color = Color(0xFFEEEEEE))
                .focusRequester(focusRequester),
            isError = uiState.errorMessage != null,
            supportingText = uiState.errorMessage?.let {
                { Text(uiTextStringResource(it)) }
            }
        )
        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onClickNext,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
        ) {
            Text(text = stringResource(Res.string.next))
        }
    }
}