package world.respect.datalayer.db.school.entities

import androidx.room.Entity
import io.ktor.http.Url
import world.respect.datalayer.school.model.StatusEnum
import kotlin.time.Instant

@Entity(
    primaryKeys = ["bPersonUidHash", "bUrl"]
)

data class BookmarkEntity(
    val bPersonUid: String,
    val bPersonUidHash : Long,
    val bUrl: Url,
    val bUrlHash:Long,
    val bStatus: StatusEnum = StatusEnum.ACTIVE,
    val bLastModified: Instant,
    val bStored: Instant,
    val bAppManifestUrl: Url
)