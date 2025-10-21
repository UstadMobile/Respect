package world.respect.datalayer.db.school.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.ktor.http.Url
import world.respect.datalayer.school.model.StatusEnum
import kotlin.time.Instant

@Entity
data class SchoolAppEntity(
    val saUid: String,
    @PrimaryKey
    val saUidNum: Long,
    val saManifestUrl: Url,
    val saStatus: StatusEnum,
    val saLastModified: Instant,
    val saStored: Instant,
)
