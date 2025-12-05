package world.respect.app.view.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import world.respect.shared.viewmodel.settings.SharedDeviceSettingsUiState
import world.respect.shared.viewmodel.settings.SharedDeviceSettingsViewModel

@Composable
fun SharedDeviceSettingsScreen(
    viewModel: SharedDeviceSettingsViewModel
) {
    val uiState: SharedDeviceSettingsUiState by viewModel.uiState.collectAsState(
        SharedDeviceSettingsUiState()
    )

    SharedDeviceSettingsScreen(
        uiState = uiState,
        onToggleRequireRollNumber = viewModel::toggleRequireRollNumber,
        onClickNext = viewModel::onClickNext
    )
}

@Composable
fun SharedDeviceSettingsScreen(
    uiState: SharedDeviceSettingsUiState,
    onToggleRequireRollNumber: (Boolean) -> Unit = {},
    onClickNext: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Toggle Row Section
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.PhoneAndroid,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 16.dp),
                )
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Require students to enter their roll number",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "This will make students to enter their roll number after selecting their name",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Switch(
                    checked = uiState.requireRollNumber,
                    onCheckedChange = onToggleRequireRollNumber,
                    modifier = Modifier.scale(0.6f)
                )
            }

            // Next Button
            Button(
                onClick = onClickNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
            ) {
                Text(
                    text = "Next",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                )
            }
        }
    }
}