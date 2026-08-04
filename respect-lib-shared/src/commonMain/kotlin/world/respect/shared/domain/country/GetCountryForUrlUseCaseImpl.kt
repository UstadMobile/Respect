package world.respect.shared.domain.country

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.Url
import io.ktor.http.encodeURLParameter
import kotlinx.serialization.Serializable
import java.util.concurrent.ConcurrentHashMap

class GetCountryForUrlUseCaseImpl(
    private val httpClient: HttpClient,
    private val geolocationEndpoint: String
) : GetCountryForUrlUseCase {

    private val countryCache = ConcurrentHashMap<String, String?>()

    override suspend operator fun invoke(schoolUrl: Url): String? {
        val schoolUrlStr = schoolUrl.toString()
        countryCache[schoolUrlStr]?.let {
            return it
        }
        if (geolocationEndpoint.isBlank()) return null
        return try {
            val host = schoolUrl.host
            val encodedHost = host.encodeURLParameter()
            val endpointUrl = "$geolocationEndpoint/json/$encodedHost"
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