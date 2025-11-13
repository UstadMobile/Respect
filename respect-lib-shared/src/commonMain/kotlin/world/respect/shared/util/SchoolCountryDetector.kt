package world.respect.shared.util
private val schoolServerDataLocations = mapOf(
    "onrespect.app" to "DE",
)

private val countryMap = mapOf(
    "US" to "United States",
    "DE" to "Germany",
    "IN" to "India",
    "SG" to "Singapore",
    "GB" to "United Kingdom",
    "FR" to "France",
    "CA" to "Canada",
    "AU" to "Australia"
)

fun detectCountryFromSchoolUrl(schoolUrl: String): CountryInfo {
    val host = try {
        io.ktor.http.Url(schoolUrl).host.lowercase()
    } catch (e: Exception) {
        schoolUrl.lowercase()
    }
    val countryCode = schoolServerDataLocations.entries
        .firstOrNull { (domain, _) -> host.contains(domain) }
        ?.value
        ?: "DE"

    val countryName = countryMap[countryCode] ?: "Germany"

    return CountryInfo(
        countryCode = countryCode,
        countryName = countryName,
        flagEmoji = getFlagEmoji(countryCode)
    )
}

private fun getFlagEmoji(countryCode: String): String {
    val code = countryCode.uppercase()
    if (code.length != 2 || !code.all { it in 'A'..'Z' }) {
        return ""
    }

    val firstLetter = code[0]
    val secondLetter = code[1]
    val BASE_REGIONAL_INDICATOR = 0x1F1E6

    val firstIndicator = BASE_REGIONAL_INDICATOR + (firstLetter - 'A')
    val secondIndicator = BASE_REGIONAL_INDICATOR + (secondLetter - 'A')
    return String(Character.toChars(firstIndicator)) +
            String(Character.toChars(secondIndicator))
}

data class CountryInfo(
    val countryCode: String,
    val countryName: String,
    val flagEmoji: String
)