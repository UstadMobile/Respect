package world.respect.app.components


import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import org.jetbrains.compose.resources.stringResource
import world.respect.shared.domain.applanguage.SupportedLanguagesConfig
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.choose_language
import world.respect.shared.generated.resources.default_language


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageDropdown(
    selected: SupportedLanguagesConfig.UiLanguage?,
    languages: List<SupportedLanguagesConfig.UiLanguage>,
    onSelected: (SupportedLanguagesConfig.UiLanguage) -> Unit,
    enabled: Boolean
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = !expanded },
        modifier = Modifier.testTag("choose_language")
    ) {
        OutlinedTextField(
            value = getLanguageDisplayText(selected),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(Res.string.choose_language)) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
                .defaultItemPadding()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            languages.forEach { lang ->
                DropdownMenuItem(
                    text = { Text(getLanguageDisplayText(lang) ) },
                    onClick = {
                        onSelected(lang)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun getLanguageDisplayText(lang: SupportedLanguagesConfig.UiLanguage?): String {
    return if(lang?.langCode?.isEmpty() == true)
        stringResource(Res.string.default_language, lang.langDisplay)
    else
        lang?.langDisplay ?: ""
}


