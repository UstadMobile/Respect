package world.respect.shared.domain.feedback

interface CreateTicketUseCase {
    suspend operator fun invoke(ticket: FeedbackTicket
    ): ZammadTicketResponse
}