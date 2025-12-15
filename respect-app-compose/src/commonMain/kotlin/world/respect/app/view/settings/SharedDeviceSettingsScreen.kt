package world.respect.app.view.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhoneAndroid
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
import org.jetbrains.compose.resources.stringResource
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.allow_students_select_account
import world.respect.shared.generated.resources.allow_students_select_description
import world.respect.shared.generated.resources.require_roll_number
import world.respect.shared.generated.resources.require_roll_number_description
import world.respect.shared.generated.resources.school_wise_setting
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
        onToggleAllowStudentSelect = viewModel::toggleAllowStudentSelect,
        onToggleRequireRollNumber = viewModel::toggleRequireRollNumber,
    )
}

@Composable
fun SharedDeviceSettingsScreen(
    uiState: SharedDeviceSettingsUiState,
    onToggleAllowStudentSelect: (Boolean) -> Unit = {},
    onToggleRequireRollNumber: (Boolean) -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text(
            text = stringResource(Res.string.school_wise_setting),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))
        SettingToggleItem(
            title = stringResource(Res.string.allow_students_select_account),
            description = stringResource(Res.string.allow_students_select_description),
            isChecked = uiState.allowStudentSelect,
            onCheckedChange = onToggleAllowStudentSelect,
        )
        Spacer(modifier = Modifier.height(12.dp))
        SettingToggleItem(
            title = stringResource(Res.string.require_roll_number),
            description = stringResource(Res.string.require_roll_number_description),
            isChecked = uiState.requireRollNumber,
            onCheckedChange = onToggleRequireRollNumber,
        )
    }
}

@Composable
fun SettingToggleItem(
    title: String,
    description: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.PhoneAndroid,
                contentDescription = null,
                modifier = Modifier
                    .padding(end = 16.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }

        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.scale(0.6f)
        )
    }
}