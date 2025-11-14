package world.respect.shared.domain.country


import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.Url
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
            val host = url.host
            val response = httpClient.get("$GEOLOCATION_API_ENDPOINT/$host")
            val apiResponse: GeolocationApiResponse = response.body()

            val countryCode = if (apiResponse.status == "success") {
                apiResponse.countryCode
            } else {
                null
            }
            countryCache[schoolUrl] = countryCode

            countryCode
        } catch (e: Exception) {
            countryCache[schoolUrl] = null
            null
        }
    }

    @Serializable
    private data class GeolocationApiResponse(
        val status: String,
        val country: String? = null,
        val countryCode: String? = null,
        val message: String? = null
    )
}