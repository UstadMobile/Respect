package world.respect.shared.domain.account.addpasskeyusecase

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Url
import io.ktor.http.contentType
import world.respect.libutil.ext.appendEndpointSegments

class SavePersonPasskeyUseCaseClient(
    private val schoolUrl: Url,
    private val httpClient: HttpClient,
) : SavePersonPasskeyUseCase {


    override suspend fun invoke(request: SavePersonPasskeyUseCase.Request) {
        return httpClient.post(
            schoolUrl.appendEndpointSegments("api/passkey/savepersonpasskey")
        ) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

}