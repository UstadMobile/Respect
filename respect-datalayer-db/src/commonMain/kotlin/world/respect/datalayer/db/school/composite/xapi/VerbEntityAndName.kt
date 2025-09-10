package world.respect.datalayer.db.school.composite.xapi

import androidx.room.Embedded
import kotlinx.serialization.Serializable
import world.respect.datalayer.db.school.entities.xapi.VerbEntity
import world.respect.datalayer.db.school.entities.xapi.VerbLangMapEntry

@Serializable
data class VerbEntityAndName(
    @Embedded
    var verbEntity: VerbEntity = VerbEntity(),
    @Embedded
    var verbName: VerbLangMapEntry? = null,
)
