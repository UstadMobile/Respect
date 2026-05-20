package world.respect.datalayer.db.school.xapi.adapters

import world.respect.datalayer.db.school.xapi.composites.XapiAssignmentResultRow
import world.respect.lib.xapi.composites.XapiAssignmentProgress

fun XapiAssignmentResultRow.toXapiAssignmentResult(
    activityId: String,
): XapiAssignmentProgress {
    return XapiAssignmentProgress(
        activityId = activityId,
        completed = completed,
        successful = successful,
        rawScore = rawScore,
        progress = progress,
    )
}