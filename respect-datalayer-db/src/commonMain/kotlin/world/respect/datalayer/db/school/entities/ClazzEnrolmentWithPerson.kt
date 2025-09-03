package world.respect.datalayer.db.school.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable
import world.respect.datalayer.school.model.Person

/**
 * POJO representing Person and ClazzEnrolment
 */
@Serializable
class ClazzEnrolmentWithPerson : ClazzEnrolment() {

    @Embedded
    var person: Person? = null
}
