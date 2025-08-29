package world.respect.app.view.manageuser.signup

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.RespectExposedDropDownMenuField
import world.respect.app.components.RespectImageSelectButton
import world.respect.app.components.RespectLocalDateField
import world.respect.app.components.defaultItemPadding
import world.respect.app.components.uiTextStringResource
import world.respect.datalayer.oneroster.model.OneRosterGenderEnum
import world.respect.shared.util.toGenderLabel
import world.respect.shared.viewmodel.manageuser.profile.SignupUiState
import world.respect.shared.viewmodel.manageuser.profile.SignupViewModel

@Composable
fun SignupScreen(
    viewModel: SignupViewModel
) {
    val uiState by viewModel.uiState.collectAsState(Dispatchers.Main.immediate)

    SignupScreen(
        uiState = uiState,
        onFullNameChanged = viewModel::onFullNameChanged,
        onGenderChanged = viewModel::onGenderChanged,
        onDateOfBirthChanged = viewModel::onDateOfBirthChanged,
        onPersonPictureUriChanged = viewModel::onPersonPictureChanged
    )
}

@Composable
fun SignupScreen(
    uiState: SignupUiState,
    onFullNameChanged: (String) -> Unit,
    onGenderChanged: (OneRosterGenderEnum) -> Unit,
    onDateOfBirthChanged: (LocalDate?) -> Unit,
    onPersonPictureUriChanged: (String?) -> Unit = { },
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .defaultItemPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        RespectImageSelectButton(
            imageUri = uiState.personPicture,
            onImageUriChanged = onPersonPictureUriChanged,
            modifier = Modifier.size(80.dp),
        )

        OutlinedTextField(
            value = uiState.personInfo?.name?:"",
            onValueChange = onFullNameChanged,
            label = { Text(uiState.nameLabel) },
            isError = uiState.fullNameError != null,
            modifier = Modifier.fillMaxWidth(),
            supportingText = {
                uiState.fullNameError?.let{
                    Text(uiTextStringResource(it) )
                }
            }
        )

        RespectExposedDropDownMenuField(
            value = uiState.personInfo?.gender,
            label = uiState.genderLabel,
            options = OneRosterGenderEnum.entries.filterNot { it == OneRosterGenderEnum.UNSPECIFIED },
            onOptionSelected = { onGenderChanged(it) },
            itemText = { gender ->
                stringResource(gender.toGenderLabel)
            },
            isError = uiState.genderError != null,
            supportingText = {
                uiState.genderError?.let { Text(uiTextStringResource(it)) }
            },
            modifier = Modifier.fillMaxWidth()
        )


        RespectLocalDateField(
            modifier = Modifier.fillMaxWidth(),
            value = uiState.personInfo?.dateOfBirth,
            onValueChange = {onDateOfBirthChanged(it) },
            label = {
                Text(uiState.dateOfBirthLabel)
            },
            supportingText = uiState.dateOfBirthError?.let {
                { Text(uiTextStringResource(it)) }
            }
        )
    }

}
