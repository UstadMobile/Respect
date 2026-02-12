package world.respect.shared.domain.applanguage

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import world.respect.shared.domain.applanguage.SupportedLanguagesConfig.Companion.LOCALE_USE_SYSTEM

class LocaleSettingDelegateAndroid: SupportedLanguagesConfig.LocaleSettingDelegate {

    override var localeSetting: String?
        get() = AppCompatDelegate.getApplicationLocales().get(0)?.language ?: "en"
        set(value) {
            val localeList = if(value == LOCALE_USE_SYSTEM) {
                LocaleListCompat.getAdjustedDefault()
            }else {
                LocaleListCompat.forLanguageTags(value)
            }
            AppCompatDelegate.setApplicationLocales(localeList)
        }
}