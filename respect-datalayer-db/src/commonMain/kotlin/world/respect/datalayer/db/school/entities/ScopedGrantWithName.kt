package world.respect.datalayer.db.school.entities

import kotlinx.serialization.Serializable

@Serializable
class ScopedGrantWithName : ScopedGrant() {

    var name: String? = null

}