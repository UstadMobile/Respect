package world.respect.app.view.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.language
import world.respect.shared.generated.resources.loading
import world.respect.shared.generated.resources.mapping
import world.respect.shared.viewmodel.settings.SettingsViewModel

@Composable
fun SettingsScreen(
    onNavigateToLanguage: () -> Unit = {},
    onNavigateToMapping: () -> Unit = {},
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp),
    ) {
        item {
            SettingsListItem(
                icon = Icons.Filled.Language,
                title = stringResource(Res.string.language),
                onClick = onNavigateToLanguage,
                testTag = "language_setting_item"
            )

        }

        item {
            SettingsListItem(
                icon = Icons.Filled.Map,
                title = stringResource(Res.string.mapping),
                onClick = onNavigateToMapping,
                testTag = "mapping_setting_item"
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsListItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit,
    testTag: String
) {
    ListItem(
        headlineContent = {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
        },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = stringResource(Res.string.loading),
                tint = MaterialTheme.colorScheme.onSurface
            )
        },
        modifier = Modifier
            .testTag(testTag)
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        tonalElevation = 0.dp
    )
}

@Composable
fun SettingsScreenForViewModel(
    viewModel: SettingsViewModel
) {
    SettingsScreen(
        onNavigateToLanguage = viewModel::onNavigateToLanguage,
        onNavigateToMapping = viewModel::onNavigateToMapping
    )
}
