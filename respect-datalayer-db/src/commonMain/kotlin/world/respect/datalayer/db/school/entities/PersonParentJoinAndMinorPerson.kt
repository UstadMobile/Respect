package world.respect.datalayer.db.school.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable
import world.respect.datalayer.school.model.Person

@Serializable
data class PersonParentJoinAndMinorPerson(
    @Embedded
    var personParentJoin: PersonParentJoin? = null,
    @Embedded
    var minorPerson: Person? = null
)
