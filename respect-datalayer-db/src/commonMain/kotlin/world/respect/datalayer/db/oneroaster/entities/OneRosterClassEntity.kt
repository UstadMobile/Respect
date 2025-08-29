package world.respect.datalayer.db.oneroster.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for OneRosterClass
 */
@Entity
data class OneRosterClassEntity(
    @PrimaryKey
    val classSourcedId: String,
    val classStatus: String,
    val classDateLastModified: Long,
    val classTitle: String,
    val classLocation: String? = null,
    val classMetadata: String? = null,
)
