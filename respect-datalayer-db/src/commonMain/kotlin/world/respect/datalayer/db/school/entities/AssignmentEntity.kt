package world.respect.datalayer.db.school.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.time.Instant

@Entity
class AssignmentEntity(
    val aeUid: String,
    @PrimaryKey
    val aeUidNum: Long,
    val aeTitle: String,
    val aeDescription: String,
    val aeDeadline: Instant,
    val aeLastModified: Instant,
    val aeStored: Instant,
)