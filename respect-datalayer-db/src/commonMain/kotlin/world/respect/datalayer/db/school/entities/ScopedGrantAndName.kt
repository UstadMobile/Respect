package world.respect.datalayer.db.school.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

@Serializable
class ScopedGrantAndName {

    @Embedded
    var scopedGrant: ScopedGrant? = null

    var name: String? = null

}