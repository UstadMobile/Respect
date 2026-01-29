package world.respect.datalayer.sharefeedback.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FeedbackTicket(
    val title: String,

    @SerialName("group_id")
    val groupId: String,

    @SerialName("customer_id")
    val customerId: String,

    val article: Article
)

@Serializable
data class Article(
    val subject: String,
    val body: String,
    val type: String = "note"
)