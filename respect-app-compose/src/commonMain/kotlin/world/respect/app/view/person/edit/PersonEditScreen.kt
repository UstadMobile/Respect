package world.respect.app.view.person.edit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.RespectGenderExposedDropDownMenuField
import world.respect.app.components.RespectLocalDateField
import world.respect.app.components.RespectPhoneNumberTextField
import world.respect.app.components.defaultItemPadding
import world.respect.app.components.uiTextStringResource
import world.respect.datalayer.ext.dataOrNull
import world.respect.datalayer.school.model.Person
import world.respect.datalayer.school.model.PersonGenderEnum
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.date_of_birth
import world.respect.shared.generated.resources.email
import world.respect.shared.generated.resources.first_names
import world.respect.shared.generated.resources.last_name
import world.respect.shared.generated.resources.phone_number
import world.respect.shared.generated.resources.required
import world.respect.shared.viewmodel.person.edit.PersonEditUiState
import world.respect.shared.viewmodel.person.edit.PersonEditViewModel

@Composable
fun PersonEditScreen(
    viewModel: PersonEditViewModel,
) {
    val uiState by viewModel.uiState.collectAsState(Dispatchers.Main.immediate)
    PersonEditScreen(
        uiState = uiState,
        onEntityChanged = viewModel::onEntityChanged,
        onNationalNumberSetChanged = viewModel::onNationalPhoneNumSetChanged,
    )
}

@Composable
fun PersonEditScreen(
    uiState: PersonEditUiState,
    onEntityChanged: (Person) -> Unit,
    onNationalNumberSetChanged: (Boolean) -> Unit,
) {
    val person = uiState.person.dataOrNull()
    val fieldsEnabled = uiState.fieldsEnabled

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            modifier = Modifier.testTag("first_names")
                .fillMaxWidth().defaultItemPadding(top = 16.dp),
            value = person?.givenName ?: "",
            label = { Text(stringResource(Res.string.first_names) + "*") },
            onValueChange = { value ->
                person?.also {
                    onEntityChanged(it.copy(givenName = value))
                }
            },
            singleLine = true,
            enabled = fieldsEnabled,
            supportingText = {
                Text(stringResource(Res.string.required))
            }
        )

        OutlinedTextField(
            modifier = Modifier.testTag("last_name").fillMaxWidth().defaultItemPadding(),
            value = person?.familyName ?: "",
            label = { Text(stringResource(Res.string.last_name) + "*") },
            onValueChange = { value ->
                person?.also {
                    onEntityChanged(it.copy(familyName = value))
                }
            },
            singleLine = true,
            supportingText = {
                Text(stringResource(Res.string.required))
            }
        )

        RespectGenderExposedDropDownMenuField(
            modifier = Modifier.testTag("gender").fillMaxWidth().defaultItemPadding(),
            value = person?.gender ?: PersonGenderEnum.UNSPECIFIED,
            onValueChanged = { gender ->
                person?.also {
                    onEntityChanged(it.copy(gender = gender))
                }
            },
            isError = uiState.genderError != null,
        )

        RespectLocalDateField(
            modifier = Modifier.testTag("date_of_birth").fillMaxWidth().defaultItemPadding(),
            value = person?.dateOfBirth,
            label = { Text(stringResource(Res.string.date_of_birth)) },
            onValueChange = { date ->
                person?.also {
                    onEntityChanged(it.copy(dateOfBirth = date))
                }
            },
            enabled = uiState.fieldsEnabled,
            supportingText = uiState.dateOfBirthError?.let {
                { Text(uiTextStringResource(it)) }
            }
        )

        RespectPhoneNumberTextField(
            value = person?.phoneNumber ?: "",
            modifier = Modifier.testTag("phone_number").fillMaxWidth().defaultItemPadding(),
            label = { Text(stringResource(Res.string.phone_number)) },
            onValueChange = { phoneNumber ->
                person?.also {
                    onEntityChanged(it.copy(phoneNumber = phoneNumber))
                }
            },
            onNationalNumberSetChanged = onNationalNumberSetChanged,
            isError = uiState.phoneNumError != null,
            supportingText = uiState.phoneNumError?.let {
                { Text(uiTextStringResource(it)) }
            },
        )

        OutlinedTextField(
            modifier = Modifier.testTag("email").fillMaxWidth().defaultItemPadding(),
            value = person?.email ?: "",
            label = { Text(stringResource(Res.string.email)) },
            singleLine = true,
            onValueChange = { email ->
                person?.also {
                    onEntityChanged(it.copy(email = email))
                }
            },
            enabled = uiState.fieldsEnabled
        )
    }

}
