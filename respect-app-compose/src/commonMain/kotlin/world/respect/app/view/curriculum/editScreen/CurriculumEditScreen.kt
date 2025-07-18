package world.respect.app.view.curriculum.editScreen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import world.respect.app.viewmodel.CurriculumEditViewModel

@Composable
fun CurriculumEditScreenWrapper(
    viewModel: CurriculumEditViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CurriculumEditScreen(
        uiState = uiState,
        onNameChange = viewModel::onNameChange,
        onIdChange = viewModel::onIdChange,
        onDescriptionChange = viewModel::onDescriptionChange,
        onBackClick = viewModel::onBackClick,
        onSaveClick = viewModel::onSaveClick
    )
}
@Composable
fun CurriculumEditScreen(
    uiState: CurriculumEditViewModel.CurriculumEditUiState = CurriculumEditViewModel.CurriculumEditUiState(),
    onNameChange: (String) -> Unit = {},
    onIdChange: (String) -> Unit = {},
    onDescriptionChange: (String) -> Unit = {},
    onBackClick: () -> Unit = {},
    onSaveClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = if (uiState.isEditMode) {
                        stringResource(Res.string.edit_curriculum)
                    } else {
                        stringResource(Res.string.add_curriculum)
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            OutlinedButton(
                onClick = onSaveClick,
                enabled = !uiState.isLoading,
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.height(32.dp)
            ) {
                Text(
                    text = stringResource(Res.string.save),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = uiState.name,
                onValueChange = onNameChange,
                label = { Text(stringResource(Res.string.name)) },
                modifier = Modifier
                    .fillMaxWidth(),
                singleLine = true,
                isError = uiState.nameError != null,
                enabled = !uiState.isLoading
            )
            uiState.nameError?.let { nameError ->
                Text(
                    text = stringResource(Res.string.name_required_error),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
            OutlinedTextField(
                value = uiState.id,
                onValueChange = onIdChange,
                label = { Text(stringResource(Res.string.id)) },
                modifier = Modifier
                    .fillMaxWidth(),
                isError = uiState.idError != null,
                enabled = !uiState.isLoading
            )
            uiState.idError?.let { idError ->
                Text(
                    text = stringResource(Res.string.id_required_error),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
            OutlinedTextField(
                value = uiState.description,
                onValueChange = onDescriptionChange,
                label = { Text(stringResource(Res.string.description)) },
                modifier = Modifier
                    .fillMaxWidth(),
                isError = uiState.descriptionError != null,
                enabled = !uiState.isLoading
            )
            uiState.descriptionError?.let { descriptionError ->
                Text(
                    text = stringResource(Res.string.description_required_error),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        uiState.error?.let { errorMessage ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}