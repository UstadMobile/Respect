package world.respect.lib.xapi.model

import kotlinx.serialization.Serializable
import kotlin.time.Instant

/**
 * A summary of an assignment for list display.
 */
@Serializable
data class AssignmentSummary(
    val activityId: String,
    val title: String,
    val className: String,
    val lastModified: Instant,
    val deadline: Instant?,
    val completedCount: Int,
    val totalCount: Int,
    val learningUnitManifestUrls: List<String> = emptyList(),
    val statementId: String = ""
) {
    val isCompleted: Boolean get() = totalCount > 0 && completedCount == totalCount
}
