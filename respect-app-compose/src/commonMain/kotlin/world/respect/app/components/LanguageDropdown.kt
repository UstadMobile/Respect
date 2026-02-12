package world.respect.app.components


import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import world.respect.shared.generated.resources.language
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import world.respect.shared.domain.applanguage.SupportedLanguagesConfig
import world.respect.shared.generated.resources.Res


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
        onExpandedChange = { if (enabled) expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selected?.langDisplay ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(Res.string.language)) },
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
                    text = { Text(lang.langDisplay) },
                    onClick = {
                        onSelected(lang)
                        expanded = false
                    }
                )
            }
        }
    }
}


