package world.respect.app.components

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import world.respect.shared.resources.LangMapUiText
import world.respect.shared.resources.StringResourceUiText
import world.respect.shared.resources.StringUiText
import world.respect.shared.resources.UiText

@Composable
fun uiTextStringResource(uiText: UiText): String {
    return when(uiText) {
        is StringResourceUiText -> {
            stringResource(uiText.resource)
        }

        is LangMapUiText -> {
            uiText.langMap.entries.firstOrNull {
                it.key == LocalAppLocale.current
            }?.value ?: uiText.langMap.entries.firstOrNull()?.value ?: ""
        }

        is StringUiText -> uiText.text
    }
}
