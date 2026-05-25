package world.respect.datalayer.db.school.xapi.composites


data class XapiSummaryResultRow(
    val activityUid: Long,
    val activityId: String,
    val title: String?,
    val numCompleted: Int,
    val numTotal: Int,
    val deadlineStr: String?,
)
