package world.respect.shared.domain.account.authwithopenid

import io.ktor.http.Url

data class OpenIdAuthorizationResult(
    val requestId: String,
    val issuer: Url,
    val authorizationCode: String? = null,
    val accessToken: String? = null,
    val idToken: String? = null,
    val errorMessage: String? = null,
)

interface OpenIdAuthorizationUseCase {

    suspend operator fun invoke(issuer: Url): OpenIdAuthorizationResult
}
