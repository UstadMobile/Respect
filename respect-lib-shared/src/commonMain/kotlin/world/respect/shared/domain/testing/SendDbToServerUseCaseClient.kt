package world.respect.shared.domain.testing

import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Url
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType
import io.ktor.http.takeFrom

class SendDbToServerUseCaseClient(
    private val httpClient: HttpClient,
    private val getDbFilesForUploadUseCase: GetDbFilesForUploadUseCase,
) : SendDbToServerUseCase {

    override suspend fun invoke(schoolUrl: Url, name: String) {
        val file = getDbFilesForUploadUseCase(schoolUrl) ?: return
        httpClient.post {
            url {
                takeFrom(schoolUrl)
                appendPathSegments("api/e2e/receivedb")
            }
            parameter("name", name)
            contentType(ContentType.Application.OctetStream)
            setBody(file.bytes)
        }
    }
}
