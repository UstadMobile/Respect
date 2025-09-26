package world.respect.shared.domain.account.gettokenanduser

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Url
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType
import io.ktor.http.takeFrom
import world.respect.credentials.passkey.RespectCredential
import world.respect.credentials.passkey.RespectPasswordCredential
import world.respect.shared.domain.account.AuthResponse

class GetTokenAndUserProfileWithCredentialUseCaseClient(
    private val schoolUrl: Url,
    private val httpClient: HttpClient,
): GetTokenAndUserProfileWithCredentialUseCase {

    override suspend fun invoke(
        credential: RespectCredential
    ): AuthResponse {
        return httpClient.post {
            url {
                takeFrom(schoolUrl)
                appendPathSegments("api/school/respect/auth/auth-with-password")
            }
            when(credential) {
                is RespectPasswordCredential -> {
                    parameter(
                        GetTokenAndUserProfileWithCredentialUseCase.PARAM_NAME_USERNAME,
                        credential.username
                    )
                    contentType(ContentType.Text.Plain)
                    setBody(credential.password)
                }
                else -> {
                    TODO()
                }
            }
        }.body()
    }
}