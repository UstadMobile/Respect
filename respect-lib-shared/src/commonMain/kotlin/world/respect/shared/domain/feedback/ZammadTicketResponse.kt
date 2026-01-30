package world.respect.shared.domain.feedback

import kotlinx.serialization.Serializable

@Serializable
data class ZammadTicketResponse(
    val id: Int,
    val number: String,
    val title: String,
    val customer_id: Int?,
    val created_at: String
)