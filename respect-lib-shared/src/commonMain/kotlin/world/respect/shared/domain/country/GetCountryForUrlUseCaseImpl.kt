package world.respect.shared.domain.country


import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.http.contentType
import io.ktor.http.path
import kotlinx.serialization.Serializable
import java.util.concurrent.ConcurrentHashMap

class GetCountryForUrlUseCaseImpl(
    private val httpClient: HttpClient
) : GetCountryForUrlUseCase {

    private val countryCache = ConcurrentHashMap<String, String?>()

    companion object {
        private const val GEOLOCATION_API_ENDPOINT = "http://ip-api.com/json"
    }

    override suspend operator fun invoke(schoolUrl: String): String? {
        countryCache[schoolUrl]?.let {
            return it
        }

        return try {
            val url = Url(schoolUrl)

            val endpointUrl = URLBuilder(GEOLOCATION_API_ENDPOINT)
                .apply { path(url.host) }
                .build()

            val response = httpClient.get(endpointUrl) {
                contentType(ContentType.Application.Json)
            }

            val apiResponse: GeolocationApiResponse = response.body()

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
    private data class GeolocationApiResponse(
        val status: String,
        val countryCode: String? = null,
        val message: String? = null
    )
}