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

    override suspend operator fun invoke(schoolUrl: Url): String? {
        val schoolUrlStr = schoolUrl.toString()
        countryCache[schoolUrlStr]?.let {
            return it
        }

        return try {
            val host = schoolUrl.host

            val encodedHost = host.encodeURLParameter()
            val endpointUrl = "$GEOLOCATION_API_ENDPOINT/api/country/$encodedHost"

            val response = httpClient.get(endpointUrl)
            val apiResponse: CountryResponse = response.body()

            val countryCode = if (apiResponse.status == "success") {
                apiResponse.countryCode ?: "Unknown"
            } else {
                "Unknown"
            }

            countryCache[schoolUrlStr] = countryCode
            countryCode

        } catch (e: Exception) {
            countryCache[schoolUrlStr] = "unknown"
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