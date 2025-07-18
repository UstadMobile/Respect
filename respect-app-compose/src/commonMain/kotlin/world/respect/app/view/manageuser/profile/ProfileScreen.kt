package world.respect.app.view.manageuser.profile

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import kotlinx.datetime.LocalDate
import world.respect.app.app.dateofbirth.DateOfBirthSelector
import world.respect.app.components.RespectExposedDropDownMenuField
import world.respect.app.components.defaultItemPadding
import world.respect.app.components.uiTextStringResource
import world.respect.datalayer.oneroster.rostering.model.OneRosterGenderEnum
import world.respect.shared.viewmodel.manageuser.profile.ProfileUiState
import world.respect.shared.viewmodel.manageuser.profile.ProfileViewModel

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    ProfileScreen(
        uiState = uiState,
        onFullNameChanged = viewModel::onFullNameChanged,
        onGenderChanged = viewModel::onGenderChanged,
        onDateOfBirthChanged = viewModel::onDateOfBirthChanged,
    )
}

@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    onFullNameChanged: (String) -> Unit,
    onGenderChanged: (OneRosterGenderEnum) -> Unit,
    onDateOfBirthChanged: (LocalDate?) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .defaultItemPadding()
    ) {


        item {
            OutlinedTextField(
                value = uiState.fullName,
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
        }

        item {
            RespectExposedDropDownMenuField(
                value = uiState.gender,
                label = uiState.genderLabel,
                options = OneRosterGenderEnum.entries.filterNot { it==OneRosterGenderEnum.UNSPECIFIED },
                onOptionSelected = { onGenderChanged(it) },
                itemText = { gender ->
                    gender.name
                },
                isError = uiState.genderError != null,
                supportingText = {
                    uiState.genderError?.let { Text(uiTextStringResource(it)) }
                },
                modifier = Modifier.fillMaxWidth()
            )

        }

        item {

            DateOfBirthSelector(
                date = uiState.dateOfBirth,
                onDateChanged = {onDateOfBirthChanged(it) },
                label = uiState.dateOfBirthLabel,
                error = uiState.dateOfBirthError
            )
        }
    }
}
