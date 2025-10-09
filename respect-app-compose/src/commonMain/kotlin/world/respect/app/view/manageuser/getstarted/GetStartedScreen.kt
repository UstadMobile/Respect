package world.respect.app.view.manageuser.getstarted

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.RespectShortVersionInfoText
import world.respect.app.components.defaultItemPadding
import world.respect.app.components.uiTextStringResource
import world.respect.datalayer.respect.model.SchoolDirectoryEntry
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.enter_school_name
import world.respect.shared.generated.resources.other_options
import world.respect.shared.generated.resources.school_name_placeholder
import world.respect.shared.viewmodel.app.appstate.getTitle
import world.respect.shared.viewmodel.manageuser.getstarted.GetStartedUiState
import world.respect.shared.viewmodel.manageuser.getstarted.GetStartedViewModel

@Composable
fun GetStartedScreen(
    viewModel: GetStartedViewModel
) {
    val uiState by viewModel.uiState.collectAsState(context = Dispatchers.Main.immediate)

    GetStartedScreen(
        uiState = uiState,
        onSchoolNameChanged = viewModel::onSchoolNameChanged,
        onClickOtherOptions = viewModel::onClickOtherOptions,
        onSchoolSelected = viewModel::onSchoolSelected
    )
}

@Composable
fun GetStartedScreen(
    uiState: GetStartedUiState,
    onSchoolNameChanged: (String) -> Unit,
    onSchoolSelected: (SchoolDirectoryEntry) -> Unit,
    onClickOtherOptions: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .defaultItemPadding()
    ) {
        uiState.warning?.also {
            Row(Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                )

                Spacer(Modifier.width(16.dp))

                Text(
                    text = uiTextStringResource(it),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        uiState.errorText?.let {
            Text(it)
        }

        OutlinedTextField(
            value = uiState.schoolName,
            onValueChange = onSchoolNameChanged,
            label = {
                Text(text = stringResource(Res.string.enter_school_name))
            },
            placeholder = {
                Text(text = stringResource(Res.string.school_name_placeholder))
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            modifier = Modifier.testTag("school_name")
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .testTag("school_name"),
            isError = uiState.errorMessage != null,
            supportingText = uiState.errorMessage?.let {
                { Text(uiTextStringResource(it)) }
            }
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth().testTag("schools_list")
        ) {
            items(
                count = uiState.suggestions.size,
                key = { index -> uiState.suggestions[index].self.toString() }
            ) { index ->
                val school = uiState.suggestions[index]
                ListItem(
                    headlineContent = {
                        Text(
                            text = school.name.getTitle()
                        )
                    },
                    supportingContent = {
                        Text(
                            text = school.self.toString(),
                            maxLines = 1
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                        .clickable { onSchoolSelected(school) }
                )
            }
        }

        if (uiState.showButtons){
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(
                onClick = onClickOtherOptions,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(Res.string.other_options))
            }
        }

        RespectShortVersionInfoText(Modifier.defaultItemPadding().fillMaxWidth())
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}
