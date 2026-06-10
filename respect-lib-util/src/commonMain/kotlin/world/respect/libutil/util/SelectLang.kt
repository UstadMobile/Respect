package world.respect.libutil.util

private const val INDEX_END_LANG_CODE = 2

private const val INDEX_START_COUNTRY_CODE = 3

/**
 * The two character locale language code: always the first two characters.
 */
fun String.localeLanguage(): String {
    return substring(0, INDEX_END_LANG_CODE)
}

/**
 * The two character country code, if present, always from the 3rd character
 */
fun String.localeCountry(): String? {
    return if(length > INDEX_START_COUNTRY_CODE){
        substring(INDEX_START_COUNTRY_CODE)
    }else {
        null
    }
}

/**
 * Given a list user preferred locales (e.g. a user prefers Arabic (AE), then English (US))
 * And a list of the locales in which a particular resource is available, select the preferred
 * locale.
 *
 * If nothing suitable is available (availableLocales is empty), returns null
 */
fun selectLangOrNull(
    preferredLocales: List<String>,
    availableLocales: List<String>,
): String? {
    if(availableLocales.isEmpty())
        return null

    if(availableLocales.size == 1)
        return availableLocales.first()

    return preferredLocales.firstOrNull { preferredLocale ->
        availableLocales.any {
            (it.localeLanguage() == preferredLocale.localeLanguage()) &&
                    (it.localeCountry() == preferredLocale.localeCountry())
        }
    } ?: preferredLocales.firstOrNull { preferredLocale ->
        availableLocales.any {
            it.localeCountry() == preferredLocale.localeCountry()
        }
    } ?: availableLocales.firstOrNull()
}

/**
 * Given a list user preferred locales (e.g. a user prefers Arabic (AE), then English (US))
 * And a list of the locales in which a particular resource is available, select the preferred
 * locale.
 */
fun selectLang(
    preferredLocales: List<String>,
    availableLocales: List<String>,
): String {
    return selectLangOrNull(preferredLocales, availableLocales)
        ?: throw IllegalStateException("Could not select lang: available=$availableLocales, preferred=$preferredLocales")
}
