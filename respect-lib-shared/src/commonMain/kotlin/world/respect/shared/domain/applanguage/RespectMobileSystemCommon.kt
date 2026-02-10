package world.respect.shared.domain.applanguage

import org.jetbrains.compose.resources.StringResource
import com.russhwolf.settings.Settings


abstract class RespectMobileSystemCommon(
    private val settings: Settings,
    protected val langConfig: SupportedLanguagesConfig,
) {

    data class UiLanguage(val langCode: String, val langDisplay: String)


    abstract fun setSystemLocale(langCode: String)

    abstract fun getString(stringResource: StringResource): String

    abstract fun formatString(
        stringResource: StringResource,
        vararg args: Any
    ): String


    companion object {

        /**
         * The return value from getLocale when the user has said to use the system's locale
         */
        const val LOCALE_USE_SYSTEM = ""

    }
}