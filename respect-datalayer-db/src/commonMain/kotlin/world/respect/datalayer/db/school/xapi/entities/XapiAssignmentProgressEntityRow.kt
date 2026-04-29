package world.respect.datalayer.db.school.xapi.entities

/**
 * A row representing the progress of a person on an assignment activity.
 * This is used as a projection for Room queries in [world.respect.datalayer.db.school.xapi.daos.XapiStatementEntityDao].
 */
data class XapiAssignmentProgressEntityRow(
    val personUid: String,
    val activityId: String,
    val completion: Boolean?,
    val success: Boolean?,
    val scoreScaled: Float?,
    val progress: Int?
)
