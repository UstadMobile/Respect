package world.respect.datalayer.http.sharefeedback

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import world.respect.datalayer.sharefeedback.model.FeedbackTicket
import world.respect.datalayer.sharefeedback.FeedBackDataSource

class FeedbackDataSourceHttp(
    private val httpClient: HttpClient,
    private val zammadToken: String,
    private val zammadUrl: String
) : FeedBackDataSource {

    override suspend fun createTicket(ticket: FeedbackTicket): HttpResponse {

        val response =httpClient.post(zammadUrl) {
            headers[HttpHeaders.Authorization] = zammadToken
            contentType(ContentType.Application.Json)
            setBody(ticket)
        }
        return response
    }
}