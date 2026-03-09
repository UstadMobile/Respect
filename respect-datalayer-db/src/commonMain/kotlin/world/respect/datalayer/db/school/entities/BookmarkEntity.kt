package world.respect.datalayer.db.school.entities

import androidx.room.Entity
import world.respect.datalayer.school.model.StatusEnum
import kotlin.time.Instant

@Entity(
    primaryKeys = ["bPersonUidHash", "bLearningUnitUrlHash"]
)

data class BookmarkEntity(
    val bPersonUid: String,
    val bPersonUidHash : Long,
    val bLearningUnitManifestUrl: String,
    val bLearningUnitUrlHash:Long,
    val bStatus: StatusEnum = StatusEnum.ACTIVE,
    val bLastModified: Instant,
    val bStored: Instant
)