package world.respect.datalayer.db.school.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable
import world.respect.datalayer.school.model.Person

@Serializable
data class PersonAndDisplayDetail(
    @Embedded
    var person: Person? = null,

    @Embedded
    var parentJoin: PersonParentJoin? = null,

    @Embedded
    var personPicture: PersonPicture? = null,

    @Embedded
    var personPictureTransferJobItem: TransferJobItem? = null
)