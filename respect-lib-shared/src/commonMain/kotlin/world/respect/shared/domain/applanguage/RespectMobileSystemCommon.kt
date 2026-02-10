package world.respect.shared.domain.applanguage

import org.jetbrains.compose.resources.StringResource
import com.russhwolf.settings.Settings

abstract class RespectMobileSystemCommon(
    protected val settings: Settings,
    protected val langConfig: SupportedLanguagesConfig,
) {

    data class UiLanguage(
        val langCode: String,
        val langDisplay: String
    )

    // Domain-level hook only (no UI side effects)
    abstract fun setSystemLocale(langCode: String)

    abstract fun getString(stringResource: StringResource): String

    abstract fun formatString(
        stringResource: StringResource,
        vararg args: Any
    ): String

    companion object {
        const val LOCALE_USE_SYSTEM = ""
    }
}
