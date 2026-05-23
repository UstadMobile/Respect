package world.respect.datalayer.db.school.xapi.composites

class XapiAssignmentResultRow(
    val actorUid: Long,
    val activityUid: Long,
    val progress: Int?,
    val completed: Boolean?,
    val successful: Boolean?,
    val scoreScaled: Float?,
)