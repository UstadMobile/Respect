package world.respect.shared.domain.country

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.Url
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import java.util.concurrent.ConcurrentHashMap

class GetCountryForUrlUseCaseImpl(
    private val httpClient: HttpClient
) : GetCountryForUrlUseCase {

    private val countryCache = ConcurrentHashMap<String, String?>()

    companion object {
        // For local testing: "http://localhost:8080/country"
        private const val GEOLOCATION_API_ENDPOINT = "http://localhost:8080/country"
    }

    override suspend operator fun invoke(schoolUrl: String): String? {
        countryCache[schoolUrl]?.let {
            return it
        }

        return try {
            val url = Url(schoolUrl)
            val host = url.host

            val response = httpClient.get(GEOLOCATION_API_ENDPOINT) {
                header("X-Forwarded-For", host)
                contentType(ContentType.Application.Json)
            }

            val apiResponse: GeolocationApiResponse = response.body()
            val countryCode = apiResponse.country

            countryCache[schoolUrl] = countryCode

            countryCode
        } catch (e: Exception) {
            countryCache[schoolUrl] = "unknown"
            "unknown"
        }
    }

    @Serializable
    private data class GeolocationApiResponse(
        val country: String
    )
}