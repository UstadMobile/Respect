package world.respect.libutil.util

fun String.localeLanguage(): String {
    return substring(0, 2)
}

fun String.localeCountry(): String? {
    return if(length > 3){
        substring(3)
    }else {
        null
    }
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
    return preferredLocales.firstOrNull { preferredLocale ->
         availableLocales.any {
             (it.localeLanguage() == preferredLocale.localeLanguage()) &&
                     (it.localeCountry() == preferredLocale.localeCountry())
         }
    } ?: preferredLocales.firstOrNull { preferredLocale ->
        availableLocales.any {
            it.localeCountry() == preferredLocale.localeCountry()
        }
    } ?: availableLocales.first()
}
