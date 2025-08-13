package world.respect.app.view.clazz.edit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import world.respect.shared.viewmodel.clazz.edit.ClazzEditViewModel
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.class_name_label
import world.respect.shared.viewmodel.clazz.edit.ClazzEditUiState
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import world.respect.app.components.editScreenPadding
import world.respect.datalayer.oneroster.rostering.model.OneRosterClass
import world.respect.shared.generated.resources.description
import world.respect.shared.generated.resources.end_date_label
import world.respect.shared.generated.resources.required
import world.respect.shared.generated.resources.start_date_label
import kotlin.time.ExperimentalTime


@Composable
fun ClazzEditScreen(
    viewModel: ClazzEditViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    ClazzEditScreen(
        uiState = uiState,
        onClazzChanged = viewModel::onClazzChanged,
    )
}

@OptIn(ExperimentalTime::class)
@Composable
fun ClazzEditScreen(
    uiState: ClazzEditUiState,
    onClazzChanged: (OneRosterClass) -> Unit = {},

    ) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
    ) {

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth().editScreenPadding(),
            value = uiState.entity?.title ?: "",
            label = {
                Text(
                    stringResource(Res.string.class_name_label)
                )
            },
            isError = uiState.clazzNameError != null,
            singleLine = true,
            supportingText = {
                Text(uiState.clazzNameError ?: stringResource(Res.string.required))
            },
            onValueChange = { newValue ->
                uiState.entity?.let { current ->
                    onClazzChanged(
                        current.copy(title = newValue)
                    )
                }
            }
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth().editScreenPadding(),
            value = uiState.entity?.location ?: "",
            label = {
                Text(
                    stringResource(Res.string.description)
                )
            },
            singleLine = true,
            onValueChange = { newValue ->
                uiState.entity?.let { current ->
                    onClazzChanged(
                        current.copy(location = newValue)
                    )
                }
            }
        )

          Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(16.dp)
          ) {

              OutlinedTextField(
                  value = "",
                  onValueChange = {
                      onClazzChanged
                  },
                  label = {
                      Text(
                          text = stringResource(Res.string.start_date_label)
                      )
                  },
                  singleLine = true,
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                  trailingIcon = {
                      Icon(
                          imageVector = Icons.Default.CalendarToday,
                          contentDescription = stringResource(Res.string.start_date_label),
                      )
                  },
                  modifier = Modifier.weight(1f)
                      .editScreenPadding()
                      .clickable {
                      }
              )

              OutlinedTextField(
                  value = "",
                  onValueChange = {
                      onClazzChanged
                  },
                  label = {
                      Text(
                          text = stringResource(Res.string.end_date_label)
                      )
                  },
                  singleLine = true,
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                  trailingIcon = {
                      Icon(
                          imageVector = Icons.Default.CalendarToday,
                          contentDescription = stringResource(Res.string.start_date_label),
                      )
                  },
                  modifier = Modifier.weight(1f)
                        .editScreenPadding()
              )
          }
    }
}

