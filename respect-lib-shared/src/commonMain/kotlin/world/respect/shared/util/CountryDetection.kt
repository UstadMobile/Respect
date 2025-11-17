package world.respect.shared.util

/**
 * Converts country code to flag emoji using Unicode regional indicators.
 *
 * @param countryCode Two-letter ISO 3166-1 alpha-2 country code (e.g., "DE", "US")
 * @return Flag emoji string or a White Flag for Unknown or null.
 *
 * Reference: https://en.wikipedia.org/wiki/Regional_indicator_symbol
 */
fun getFlagEmoji(countryCode: String?): String {
    val WHITE_FLAG = String(Character.toChars(0x1F3F3))

    if (countryCode == null || countryCode.lowercase() == "unknown") return WHITE_FLAG

    val code = countryCode.uppercase()
    if (code.length != 2 || !code.all { it in 'A'..'Z' }) {
        return WHITE_FLAG
    }

    val firstLetter = code[0]
    val secondLetter = code[1]
    val BASE_REGIONAL_INDICATOR = 0x1F1E6
    val firstIndicator = BASE_REGIONAL_INDICATOR + (firstLetter - 'A')
    val secondIndicator = BASE_REGIONAL_INDICATOR + (secondLetter - 'A')

    return String(Character.toChars(firstIndicator)) +
            String(Character.toChars(secondIndicator))
}


