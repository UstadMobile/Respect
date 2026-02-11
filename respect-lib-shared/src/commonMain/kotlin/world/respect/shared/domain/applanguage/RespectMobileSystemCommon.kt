package world.respect.shared.domain.applanguage

import org.jetbrains.compose.resources.StringResource
import com.russhwolf.settings.Settings


abstract class RespectMobileSystemCommon(
    private val settings: Settings,
    protected val langConfig: SupportedLanguagesConfig,
) {
    data class UiLanguage(val langCode: String, val langDisplay: String)

    companion object {
        const val LOCALE_USE_SYSTEM = ""

    }
}