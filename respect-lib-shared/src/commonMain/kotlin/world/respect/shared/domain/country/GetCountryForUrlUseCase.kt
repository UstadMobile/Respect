package world.respect.shared.domain.country

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.Url
import kotlinx.serialization.Serializable
import java.util.concurrent.ConcurrentHashMap

/**
 * Gets the country code for a given school URL by looking up the server's IP location.
 * Uses a hardcoded IP geolocation endpoint.
 * Results are cached to avoid repeated network calls.
 */
class GetCountryForUrlUseCase(
    private val httpClient: HttpClient
) {
    private val countryCache = ConcurrentHashMap<String, String?>()

    companion object {
        private const val GEOLOCATION_API_ENDPOINT = "http://ip-api.com/json"
    }

    /**
     * Gets the country code for a school URL.
     *
     * @param schoolUrl The full school URL (e.g., "https://onrespect.app/")
     * @return Two-letter country code (e.g., "DE", "US") or null if unknown
     */
    suspend operator fun invoke(schoolUrl: String): String? {
        countryCache[schoolUrl]?.let {
            return it
        }

        return try {
            val url = Url(schoolUrl)
            val host = url.host

            val countryCode = lookupCountryForHost(host)
            countryCache[schoolUrl] = countryCode

            countryCode
        } catch (e: Exception) {
            countryCache[schoolUrl] = null
            null
        }
    }

    /**
     * Makes HTTP request to the hardcoded geolocation endpoint.
     *
     * @param host The hostname or IP address to lookup (e.g., "onrespect.app")
     * @return Two-letter country code or null if lookup fails
     */
    private suspend fun lookupCountryForHost(host: String): String? {
        return try {
            val response = httpClient.get("$GEOLOCATION_API_ENDPOINT/$host")
            val apiResponse: GeolocationApiResponse = response.body()

            if (apiResponse.status == "success") {
                apiResponse.countryCode
            } else {
                "unknown"
            }
        } catch (e: Exception) {
            println("Country lookup failed for $host: ${e.message}")
            "unknown"
        }
    }

    fun clearCache() {
        countryCache.clear()
    }
}

/**
 * Response model for the hardcoded geolocation API endpoint.
 * Based on ip-api.com response format.
 */
@Serializable
private data class GeolocationApiResponse(
    val status: String,
    val country: String? = null,
    val countryCode: String? = null,
    val message: String? = null
)