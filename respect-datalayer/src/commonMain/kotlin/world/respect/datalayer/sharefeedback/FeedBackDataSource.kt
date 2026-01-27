package world.respect.datalayer.sharefeedback

import world.respect.datalayer.sharefeedback.model.FeedbackTicket

interface FeedBackDataSource {
    suspend fun createTicket(
        ticket: FeedbackTicket
    )

    companion object{
        const val DEFAULT_GROUP_ID = "1"
        const val ZAMMAD_TICKET_URL = "https://respect.zammad.com/api/v1/tickets"
        const val ZAMMAD_TICKET_TOKEN ="Token token=d8DYXTdghwp8BWPEyA7ISI6Ds1uHuSjCGUiUT33ciHoeqyozLKJ3MVRPOhCrr4gB"
    }
}
