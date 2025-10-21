package world.respect.datalayer.school.model

import kotlinx.serialization.Serializable

@Serializable
data class AssignmentAssigneeRef(
    val type: AssignmentAssigneeRefTypeEnum = AssignmentAssigneeRefTypeEnum .CLASS,
    val uid: String,
)