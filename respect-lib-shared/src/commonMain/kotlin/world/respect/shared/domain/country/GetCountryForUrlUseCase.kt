package world.respect.shared.domain.country

/**
 * Gets the country code for a given school URL by looking up the server's IP location.
 */
interface GetCountryForUrlUseCase {

    /**
     * Gets the country code for a school URL.
     *
     * @param schoolUrl The full school URL (e.g., "https://onrespect.app/")
     * @return Two-letter ISO 3166-1 alpha-2 country code (e.g., "DE", "US") or null if unknown
     */
    suspend operator fun invoke(schoolUrl: String): String?

}