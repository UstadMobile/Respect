package world.respect.shared.domain.applanguage

import android.content.Context
import com.russhwolf.settings.Settings
import org.jetbrains.compose.resources.StringResource
import java.util.Locale
import java.util.Properties


actual class RespectMobileSystemImpl(
    private val context: Context,
    settings: Settings,
    langConfig: SupportedLanguagesConfig
) : RespectMobileSystemCommon(settings, langConfig) {

    actual override fun setSystemLocale(langCode: String) {
        // NO-OP on Android
        // Locale is handled via AppCompatDelegate + Compose
    }

    actual override fun getString(stringResource: StringResource): String {
        val resName = stringResource.key
            .substringAfterLast(':')
            .substringAfterLast('/')

        val id = context.resources.getIdentifier(
            resName,
            "string",
            context.packageName
        )

        return if (id != 0) context.getString(id) else resName
    }

    actual override fun formatString(
        stringResource: StringResource,
        vararg args: Any
    ): String {
        val resName = stringResource.key
            .substringAfterLast(':')
            .substringAfterLast('/')

        val id = context.resources.getIdentifier(
            resName,
            "string",
            context.packageName
        )

        return if (id != 0) context.getString(id, *args) else resName
    }
}
