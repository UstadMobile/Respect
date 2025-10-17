package world.respect.datalayer.db.school.entities

import androidx.room.Entity
import world.respect.datalayer.school.model.Assignment.AssigneeRef.AssigneeType

@Entity(
    primaryKeys = ["aarAeUidNum", "aarAeAssigneeUidNum"]
)
class AssignmentAssigneeRefEntity(
    val aarAeUidNum: Long,
    val aarType: AssigneeType,
    val aarAeAssigneeUid: String,
    val aarAeAssigneeUidNum: Long,
)
