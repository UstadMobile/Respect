package world.respect.shared.domain.country

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.Url
import io.ktor.http.encodeURLParameter
import kotlinx.serialization.Serializable
import java.util.concurrent.ConcurrentHashMap

class GetCountryForUrlUseCaseImpl(
    private val httpClient: HttpClient
) : GetCountryForUrlUseCase {

    private val countryCache = ConcurrentHashMap<String, String?>()

    companion object {
        private const val GEOLOCATION_API_ENDPOINT = "http://192.168.1.5:8080"
    }

    override suspend operator fun invoke(schoolUrl: String): String? {
        countryCache[schoolUrl]?.let {
            return it
        }

        return try {
            val url = Url(schoolUrl)
            val host = url.host

            val encodedHost = host.encodeURLParameter()
            val endpointUrl = "$GEOLOCATION_API_ENDPOINT/api/country/$encodedHost"

            val response = httpClient.get(endpointUrl)
            val apiResponse: CountryResponse = response.body()

            val countryCode = if (apiResponse.status == "success") {
                apiResponse.countryCode ?: "Unknown"
            } else {
                "Unknown"
            }

            countryCache[schoolUrl] = countryCode
            countryCode

        } catch (e: Exception) {
            countryCache[schoolUrl] = "unknown"
            "unknown"
        }
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