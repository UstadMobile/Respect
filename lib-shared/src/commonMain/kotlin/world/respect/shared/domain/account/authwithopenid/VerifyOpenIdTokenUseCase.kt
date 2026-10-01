package world.respect.shared.domain.account.authwithopenid

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.Url
import io.ktor.http.isSuccess
import io.ktor.http.appendPathSegments
import io.ktor.http.takeFrom

class VerifyOpenIdTokenUseCase(
    private val httpClient: HttpClient,
) {

    suspend operator fun invoke(
        schoolUrl: Url,
        accessToken: String,
    ): Boolean {
        val response = httpClient.get {
            url {
                takeFrom(schoolUrl)
                appendPathSegments("api/oidc/verify")
            }
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }

        return response.status.isSuccess()
    }
}
