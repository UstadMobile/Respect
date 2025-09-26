package world.respect.app.view.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.ListItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import org.jetbrains.compose.resources.stringResource
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.school_directory
import world.respect.shared.viewmodel.settings.SettingUIState
import world.respect.shared.viewmodel.settings.SettingViewModel
import androidx.compose.material3.*

data class SettingsOption(
    val title: String,
    val icon: ImageVector
)

@Composable
fun SettingScreen(
    viewModel: SettingViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()
    SettingScreen(
        uiState = uiState,
        onClickSchoolDirectory = viewModel::onClickSchoolDirectory
    )
}

@Composable
fun SettingScreen(
    uiState: SettingUIState,
    onClickSchoolDirectory: () -> Unit
) {
    val settingOptions = listOf(
        SettingsOption(
            title = stringResource(Res.string.school_directory),
            icon = Icons.Default.School
        ),
        // Add more options here if needed
    )
    LazyColumn {
        items(
            count = settingOptions.size,
            key = { index -> settingOptions[index].title }
        ) { index ->
            val option = settingOptions[index]
            SettingListItem(
                option,
                onClick = {
                    when (index) {
                        0 -> onClickSchoolDirectory()
                    }
                }
            )
        }
    }
}

@Composable
fun SettingListItem(
    option: SettingsOption,
    onClick: (SettingsOption) -> Unit
) {
    ListItem(
        modifier = Modifier.clickable { onClick(option)},
        leadingContent = {
            Icon(
                imageVector = option.icon,
                contentDescription = option.title
            )
        },
        headlineContent = {
            Text(option.title)
        },
    )
}
