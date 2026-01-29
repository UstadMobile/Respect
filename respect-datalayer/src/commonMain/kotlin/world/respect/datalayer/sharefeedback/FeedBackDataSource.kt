package world.respect.datalayer.sharefeedback

import world.respect.datalayer.sharefeedback.model.FeedbackTicket
import io.ktor.client.statement.HttpResponse

interface FeedBackDataSource {
    suspend fun createTicket(
        ticket: FeedbackTicket
    ): HttpResponse

    companion object{
        const val DEFAULT_GROUP_ID = "1"
        const val ZAMMAD_TICKET_URL = "https://respect.zammad.com/api/v1/tickets"
    }
}
