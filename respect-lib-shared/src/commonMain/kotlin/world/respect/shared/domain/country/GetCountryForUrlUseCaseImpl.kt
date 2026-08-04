package world.respect.shared.domain.country

import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.Url
import io.ktor.http.encodeURLParameter
import kotlinx.serialization.Serializable

/**
 * Gets the country for a school URL using an IP address country lookup server.
 *
 * There is deliberately no cache here: the HttpClient uses UstadCacheInterceptor, so responses
 * are cached by the HTTP cache as per the Cache-Control header set by the lookup server.
 *
 * The server implements the same JSON API format as ip-api.com.
 * Reference: https://ip-api.com/docs/api:json
 *
 * @param geolocationEndpoint Base URL of the lookup server, as per the
 *        GEOLOCATION_API_ENDPOINT build environment variable.
 */
class GetCountryForUrlUseCaseImpl(
    private val httpClient: HttpClient,
    private val geolocationEndpoint: String,
) : GetCountryForUrlUseCase {

    override suspend operator fun invoke(schoolUrl: Url): String? {
        if (geolocationEndpoint.isBlank()) {
            Napier.w("GetCountryForUrlUseCase: GEOLOCATION_API_ENDPOINT is not set")
            return null
        }

        val encodedHost = schoolUrl.host.encodeURLParameter()
        val response: CountryResponse = httpClient
            .get("$geolocationEndpoint/json/$encodedHost")
            .body()

        return response.countryCode?.takeIf { response.status == STATUS_SUCCESS }
    }

    companion object {
        private const val STATUS_SUCCESS = "success"
    }

    @Serializable
    private data class CountryResponse(
        val status: String,
        val countryCode: String? = null,
        val country: String? = null,
        val message: String? = null,
        val query: String? = null
    )
}