package world.respect.app.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import java.util.Locale
import androidx.compose.ui.platform.LocalLocale

actual object LocalAppLocale {
    actual val current: String
        @Composable get() = LocalLocale.current.platformLocale.toString()

    /**
     * When value is null, the locale is left as-is: the in-app language is applied by
     * AppCompatDelegate.setApplicationLocales (see LocaleSettingDelegateAndroid), which updates
     * the Activity configuration. Forcing a cached default here would override the user's choice.
     */
    @Composable
    actual infix fun provides(value: String?): ProvidedValue<*> {
        val configuration = LocalConfiguration.current

        if(value == null)
            return LocalConfiguration.provides(configuration)

        val new = Locale(value)
        Locale.setDefault(new)
        configuration.setLocale(new)
        val resources = LocalContext.current.resources

        resources.updateConfiguration(configuration, resources.displayMetrics)
        return LocalConfiguration.provides(configuration)
    }
}