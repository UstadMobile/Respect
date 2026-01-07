package world.respect.datalayer.db.school.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import world.respect.datalayer.school.model.StatusEnum
import kotlin.time.Instant

@Entity
data class PersonBadgeEntity(
    @PrimaryKey
    val pqrGuidNum: Long,
    val pqrGuid: String,
    val pqrLastModified: Instant,
    val pqrStored: Instant,
    val pqrQrCodeUrl: String,
    val pqrStatus: StatusEnum = StatusEnum.ACTIVE,
)
