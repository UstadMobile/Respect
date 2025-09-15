package world.respect.datalayer.db.schooldirectory.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.ktor.http.Url

/**
 * @property reUid the XXHash64 of the rrSelfUrl
 */
@Entity
data class SchoolDirectoryEntryEntity(
    @PrimaryKey
    val reUid: Long,
    val reLastMod: Long,
    val reEtag: String?,
    val reSelf: Url,
    val reXapi: Url,
    val reOneRoster: Url,
    val reRespectExt: Url?,
    val reRpId: String?,
    val reSchoolCode: String?,
)