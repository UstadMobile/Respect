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
import world.respect.app.components.getLanguageDisplayText
import world.respect.shared.domain.applanguage.SupportedLanguagesConfig
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.language
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
                        Text(getLanguageDisplayText(lang))
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
                    Text(getLanguageDisplayText(uiState.currentLanguage))
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
