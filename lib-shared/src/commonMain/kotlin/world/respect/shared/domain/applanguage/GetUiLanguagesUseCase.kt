package world.respect.shared.domain.applanguage

import world.respect.shared.domain.applanguage.SupportedLanguagesConfig.Companion.LOCALE_USE_SYSTEM

/**
 * Get the list of languages that the user can select (system default first, followed by all
 * supported languages) and the currently selected language.
 */
class GetUiLanguagesUseCase(
    private val supportedLangConfig: SupportedLanguagesConfig,
) {

    data class UiLanguagesResult(
        val availableLanguages: List<SupportedLanguagesConfig.UiLanguage>,
        val selectedLanguage: SupportedLanguagesConfig.UiLanguage,
    )

    operator fun invoke(): UiLanguagesResult {
        val availableLangs = supportedLangConfig.getAvailableLanguages()
        val langSetting = supportedLangConfig.localeSetting ?: LOCALE_USE_SYSTEM

        return UiLanguagesResult(
            availableLanguages = availableLangs,
            selectedLanguage = availableLangs.firstOrNull { it.langCode == langSetting }
                ?: availableLangs.first(),
        )
    }
}
