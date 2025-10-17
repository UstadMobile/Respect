package world.respect.datalayer.db.school.entities

import androidx.room.Entity
import world.respect.datalayer.school.model.AssignmentAssigneeRefTypeEnum

@Entity(
    primaryKeys = ["aarAeUidNum", "aarAeAssigneeUidNum"]
)
class AssignmentAssigneeRefEntity(
    val aarAeUidNum: Long,
    val aarType: AssignmentAssigneeRefTypeEnum,
    val aarAeAssigneeUid: String,
    val aarAeAssigneeUidNum: Long,
)
