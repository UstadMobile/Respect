package world.respect.shared.domain.feedback

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import world.respect.shared.BuildConfig

class CreateTicketUseCaseImpl(
    private val httpClient: HttpClient,
) : CreateTicketUseCase {

    override suspend fun invoke(ticket: FeedbackTicket): ZammadTicketResponse {
       return httpClient.post(
            BuildConfig.FEEDBACK_ZAMMADURL
        ) {
            headers[HttpHeaders.Authorization] = "Token token=${BuildConfig.FEEDBACK_ZAMMADTOKEN}"
            contentType(ContentType.Application.Json)
            setBody(ticket)
        }.body()
    }
}