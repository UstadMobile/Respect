package world.respect.datalayer.db.school.entities

import androidx.room.Embedded
import world.respect.datalayer.school.model.Person

data class UserSessionAndPerson(
    @Embedded
    var person: Person? = null,

    @Embedded
    var personPicture: PersonPicture? = null,

    @Embedded
    var userSession: UserSession? = null,

    )
