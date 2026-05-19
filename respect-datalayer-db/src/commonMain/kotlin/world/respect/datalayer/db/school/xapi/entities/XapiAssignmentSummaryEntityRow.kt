package world.respect.datalayer.db.school.xapi.entities

/**
 * A row representing a summary of an assignment from the database.
 */
data class XapiAssignmentSummaryEntityRow(
    val activityId: String,
    val title: String?,
    val className: String?,
    val lastModified: Long,
    val deadlineJson: String?,
    val completedCount: Int,
    val totalCount: Int,
    val learningUnitsConcat: String?
)
