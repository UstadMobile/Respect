package world.respect.app.view.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import org.jetbrains.compose.resources.stringResource
import world.respect.shared.domain.applanguage.SupportedLanguagesConfig
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.default_language
import world.respect.shared.generated.resources.language
import world.respect.shared.generated.resources.loading
import world.respect.shared.viewmodel.settings.SettingsUiState
import world.respect.shared.viewmodel.settings.SettingsViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    SettingsScreen(
        uiState = uiState,
        onClickLang = viewModel::onClickLang,
        onClickLanguage = viewModel::onClickLanguage,
        onDismissLangDialog = viewModel::onDismissLangDialog
    )
}

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onClickLanguage: () -> Unit = {},
    onDismissLangDialog: () -> Unit = {},
    onClickLang: (SupportedLanguagesConfig.UiLanguage) -> Unit = {}
) {

    if (uiState.langDialogVisible) {
        SettingsDialog(
            onDismissRequest = onDismissLangDialog,
        ) {
            uiState.availableLanguages.forEach { lang ->
                ListItem(
                    modifier = Modifier.clickable { onClickLang(lang) },
                    headlineContent = {
                        if (lang.langCode.isEmpty()) {
                            Text(stringResource(Res.string.default_language, lang.langDisplay))
                        } else {
                            Text(lang.langDisplay)
                        }
                    }
                )
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp),
    ) {

        item {
            ListItem(
                headlineContent = {
                    Text(text = stringResource(Res.string.language))
                },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Filled.Language,
                        contentDescription = stringResource(Res.string.language)
                    )
                },
                supportingContent = {
                    uiState.currentLanguage?.let { lang ->
                        if (lang.langCode.isEmpty()) {
                            Text(stringResource(Res.string.default_language, lang.langDisplay))
                        } else {
                            Text(lang.langDisplay)
                        }
                    }

                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        onClick = {
                            onClickLanguage()
                        }
                    )
            )
        }
    }
}

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
fun SettingsDialog(
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            content()
        }
    }
}
