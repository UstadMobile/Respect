package world.respect.datalayer.db.school.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable
import world.respect.datalayer.school.model.Person

@Serializable
class DiscussionPostWithPerson: DiscussionPost() {
    @Embedded
    var replyPerson: Person? = null


}