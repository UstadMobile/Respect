package world.respect.shared.domain.applanguage

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

@Composable
fun ProvideAppLocale(
    languageCode: String,
    content: @Composable () -> Unit
) {
    val baseContext = LocalContext.current

    val localizedContext = remember(languageCode) {
        val locale = if (languageCode.isEmpty()) {
            Locale.getDefault()
        } else {
            Locale.forLanguageTag(languageCode)
        }

        Locale.setDefault(locale)

        val config = Configuration(baseContext.resources.configuration)
        config.setLocale(locale)

        baseContext.createConfigurationContext(config)
    }

    CompositionLocalProvider(
        LocalContext provides localizedContext
    ) {
        content()
    }
}
