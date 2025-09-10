package world.respect.datalayer.db.school.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @property pGuid the uid of the person: If following a OneRoster server, this is sourcedId
 */
@Entity
data class PersonEntity(
    val pGuid: String,
    @PrimaryKey
    val pGuidHash: Long,
    val pActive: Boolean,
    val pLastModified: Long,
    val pStored: Long,
    val pUsername: String? = null,
    val pGivenName: String,
    val pFamilyName: String,
    val pMiddleName: String? = null,
)