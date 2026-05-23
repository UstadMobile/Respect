package world.respect.lib.xapi.ext

import world.respect.lib.xapi.composites.XapiActorAndAssignmentProgress
import world.respect.lib.xapi.composites.XapiAssignmentProgress

/**
 * Calculates the display percentage for an assignment progress unit.
 */
fun XapiAssignmentProgress.calculatePercentage(): Int? {
    return progress ?: scoreScaled?.let { (it * 100).toInt() }
}

val XapiActorAndAssignmentProgress.personUid: String
    get() = actor.account?.name ?: ""

val XapiActorAndAssignmentProgress.isStarted: Boolean
    get() = progress.any { it.completed == true || (it.progress ?: 0) > 0 }

fun XapiActorAndAssignmentProgress.isCompleted(unitActivityIds: List<String>): Boolean {
    if (unitActivityIds.isEmpty()) return false
    val progressMap = progress.associateBy { it.activityId }
    return unitActivityIds.all { id ->
        progressMap[id]?.completed == true
    }
}
