package world.respect.app.view.clazz.addclazz

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import world.respect.shared.viewmodel.clazz.addclazz.AddClazzViewModel
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.class_name_label
import world.respect.shared.generated.resources.description
import world.respect.shared.generated.resources.end_date_label
import world.respect.shared.generated.resources.start_date_label
import world.respect.shared.viewmodel.clazz.addclazz.AddClazzUiState


@Composable
fun AddClazzScreen(
    viewModel: AddClazzViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    AddClazzScreen(
        uiState = uiState,
        onClassNameChange = viewModel::onClassNameChange,
        onClassDescriptionChange = viewModel::onClassDescriptionChange,
        onStartDateChange = viewModel::onStartDateChange,
        onEndDateChange = viewModel::onEndDateChange
    )
}

@Composable
fun AddClazzScreen(
    uiState: AddClazzUiState,
    onClassNameChange: (String) -> Unit,
    onClassDescriptionChange: (String) -> Unit,
    onStartDateChange: (String) -> Unit,
    onEndDateChange: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            OutlinedTextField(
                value = uiState.className,
                onValueChange = onClassNameChange,
                label = {
                    Text(
                        text = stringResource(Res.string.class_name_label)
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                modifier = Modifier.fillMaxWidth()
                    .padding(
                        horizontal = 12.dp,
                        vertical = 6.dp
                    ),
            )

            OutlinedTextField(
                value = uiState.description,
                onValueChange = onClassDescriptionChange,
                label = {
                    Text(
                        text = stringResource(Res.string.description)
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                modifier = Modifier.fillMaxWidth()
                    .padding(
                        horizontal = 12.dp,
                        vertical = 6.dp
                    ),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                OutlinedTextField(
                    value = uiState.startDate,
                    onValueChange = onStartDateChange,
                    label = {
                        Text(
                            text = stringResource(Res.string.start_date_label)
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    modifier = Modifier.weight(1f)
                        .padding(
                            start = 12.dp,
                            end = 6.dp,
                            top = 6.dp,
                            bottom = 6.dp
                        ),
                )

                OutlinedTextField(
                    value = uiState.endDate,
                    onValueChange = onEndDateChange,
                    label = {
                        Text(
                            text = stringResource(Res.string.end_date_label)
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    modifier = Modifier.weight(1f)
                        .padding(
                            start = 6.dp,
                            end = 12.dp,
                            top = 6.dp,
                            bottom = 6.dp
                        ),
                )

            }

        }
    }

}