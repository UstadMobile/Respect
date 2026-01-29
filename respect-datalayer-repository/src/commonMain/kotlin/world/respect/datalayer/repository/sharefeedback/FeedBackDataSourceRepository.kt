package world.respect.datalayer.repository.sharefeedback

import world.respect.datalayer.sharefeedback.model.FeedbackTicket
import world.respect.datalayer.sharefeedback.FeedBackDataSource

class FeedBackDataSourceRepository(
    val remote: FeedBackDataSource
) : FeedBackDataSource {

    override suspend fun createTicket(ticket: FeedbackTicket) {
        remote.createTicket(ticket)
    }
}