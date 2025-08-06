package world.respect.datalayer.db.realmdirectory.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.ktor.http.Url

/**
 * @property reUid the XXHash64 of the rrSelfUrl
 */
@Entity
data class RealmEntity(
    @PrimaryKey
    val reUid: Long,
    val reSelf: Url,
    val reXapi: Url,
    val reOneRoster: Url,
    val reRespectExt: Url?,
)