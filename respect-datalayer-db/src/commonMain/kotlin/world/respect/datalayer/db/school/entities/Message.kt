package world.respect.datalayer.db.school.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(
    indices = [Index("messageSenderPersonUid", "messageToPersonUid", "messageTimestamp", name = "message_idx_send_to_time")]
)
@Serializable
data class Message(
    @PrimaryKey(autoGenerate = true)
    var messageUid: Long = 0,

    var messageSenderPersonUid: Long = 0,

    var messageToPersonUid: Long = 0,

    var messageText: String? = null,

    var messageTimestamp: Long = 0,

    var messageLct: Long = 0,
) {
    companion object{
        const val TABLE_ID = 126
    }
}