package world.respect.datalayer.db.compatibleapps.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class CompatibleAppAddJoin(
    @PrimaryKey
    val appCaeUid: Long,
    val added: Boolean,
)
