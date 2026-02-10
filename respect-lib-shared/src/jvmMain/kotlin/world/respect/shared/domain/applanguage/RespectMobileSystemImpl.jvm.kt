package world.respect.shared.domain.applanguage

import com.russhwolf.settings.Settings
import org.jetbrains.compose.resources.StringResource

actual class RespectMobileSystemImpl(
    settings: Settings,
    langConfig: SupportedLanguagesConfig
) : RespectMobileSystemCommon(settings, langConfig)  {
    actual override fun getString(stringResource: StringResource): String {
        TODO("Not yet implemented")
    }

    actual override fun formatString(
        stringResource: StringResource,
        vararg args: Any
    ): String {
        TODO("Not yet implemented")
    }

    actual override fun setSystemLocale(langCode: String) {
    }
}