package world.respect.datalayer.db.school.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable
import world.respect.datalayer.db.school.entities.xapi.StatementEntity
import world.respect.datalayer.school.model.Person

@Serializable
data class StatementEntityAndDisplayDetails(

    @Embedded
    var statement: StatementEntity? = null,

    @Embedded
    var person: Person? = null,

    )
