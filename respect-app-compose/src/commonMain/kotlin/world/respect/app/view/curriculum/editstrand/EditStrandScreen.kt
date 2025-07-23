package world.respect.app.view.curriculum.editstrand

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
import world.respect.shared.viewmodel.strand.edit.StrandEditUiState
import world.respect.shared.viewmodel.strand.edit.StrandEditViewModel

@Composable
fun EditStrandScreenWrapper(
    viewModel: StrandEditViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    EditStrandScreen(
        uiState = uiState,
        onNameChange = viewModel::onNameChange,
        onLearningObjectivesChange = viewModel::onLearningObjectivesChange,
        onOutcomesChange = viewModel::onOutcomesChange,
        onBackClick = viewModel::onBackClick,
        onSaveClick = viewModel::onSaveClick
    )
}

@Composable
fun EditStrandScreen(
    uiState: StrandEditUiState = StrandEditUiState(),
    onNameChange: (String) -> Unit = {},
    onLearningObjectivesChange: (String) -> Unit = {},
    onOutcomesChange: (String) -> Unit = {},
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
                        stringResource(Res.string.edit_strand)
                    } else {
                        stringResource(Res.string.add_strands_description)
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Column {
                OutlinedTextField(
                    value = uiState.strand?.name ?: "",
                    onValueChange = onNameChange,
                    label = { Text(stringResource(Res.string.strand)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = uiState.nameError != null,
                    enabled = !uiState.isLoading
                )
                uiState.nameError?.let { errorRes ->
                    Text(
                        text = stringResource(errorRes),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
            }

            Column {
                OutlinedTextField(
                    value = uiState.learningObjectives,
                    onValueChange = onLearningObjectivesChange,
                    label = { Text(stringResource(Res.string.learning_objectives)) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = uiState.learningObjectivesError != null,
                    enabled = !uiState.isLoading
                )
                uiState.learningObjectivesError?.let { errorRes ->
                    Text(
                        text = stringResource(errorRes),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
            }

            Column {
                OutlinedTextField(
                    value = uiState.outcomes,
                    onValueChange = onOutcomesChange,
                    label = { Text(stringResource(Res.string.outcomes)) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = uiState.outcomesError != null,
                    enabled = !uiState.isLoading
                )
                uiState.outcomesError?.let { errorRes ->
                    Text(
                        text = stringResource(errorRes),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
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

        uiState.error?.let { errorRes ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = stringResource(errorRes),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}
