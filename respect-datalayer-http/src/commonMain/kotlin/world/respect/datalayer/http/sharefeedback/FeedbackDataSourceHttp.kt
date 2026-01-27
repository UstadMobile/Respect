package world.respect.datalayer.http.sharefeedback

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import world.respect.datalayer.sharefeedback.model.FeedbackTicket
import world.respect.datalayer.sharefeedback.FeedBackDataSource
import world.respect.datalayer.sharefeedback.FeedBackDataSource.Companion.ZAMMAD_TICKET_TOKEN
import world.respect.datalayer.sharefeedback.FeedBackDataSource.Companion.ZAMMAD_TICKET_URL

class FeedbackDataSourceHttp(private val httpClient: HttpClient
) : FeedBackDataSource {

    override suspend fun createTicket(ticket: FeedbackTicket) {

        httpClient.post(ZAMMAD_TICKET_URL) {
            headers[HttpHeaders.Authorization] = ZAMMAD_TICKET_TOKEN
            contentType(ContentType.Application.Json)
            setBody(ticket)
        }
    }
}