package world.respect.shared.util

import io.github.aakira.napier.Napier

/**
 * Converts a country code to a flag emoji.
 *
 * A flag emoji is a pair of Unicode regional indicator symbols, one for each letter of the
 * ISO 3166-1 alpha-2 country code, e.g. "DE" becomes the indicator for D followed by the
 * indicator for E.
 *
 * Each regional indicator is above the range of a single Kotlin Char, so it is written as two
 * Chars: a fixed prefix, followed by a Char which counts up from the indicator for 'A'.
 *
 * References:
 * - Emoji flag sequences: https://unicode.org/reports/tr51/
 * - Regional indicator code chart: https://www.unicode.org/charts/PDF/U1F100.pdf
 * - Country codes: https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2
 *
 * @param countryCode Two-letter ISO 3166-1 alpha-2 country code (e.g., "DE", "US"), or null
 *        where the country is not known.
 * @return Flag emoji string, or an empty string where countryCode is null or is not a valid
 *         two-letter code.
 */
fun getFlagEmoji(countryCode: String?): String {
    if (countryCode == null) return ""

    val code = countryCode.uppercase()
    if (code.length != COUNTRY_CODE_LENGTH || !code.all { it in 'A'..'Z' }) {
        Napier.w("getFlagEmoji: not a valid ISO 3166-1 alpha-2 country code: $countryCode")
        return ""
    }

    return buildString {
        code.forEach { letter ->
            append(REGIONAL_INDICATOR_PREFIX)
            append(REGIONAL_INDICATOR_A + (letter - 'A'))
        }
    }
}

/**
 * First of the two Chars used to write any regional indicator symbol. This is the same for all
 * 26 letters.
 */
private const val REGIONAL_INDICATOR_PREFIX = '\uD83C'

/**
 * Second Char for REGIONAL INDICATOR SYMBOL LETTER A (U+1F1E6). B, C, D etc follow sequentially.
 */
private const val REGIONAL_INDICATOR_A = '\uDDE6'

private const val COUNTRY_CODE_LENGTH = 2