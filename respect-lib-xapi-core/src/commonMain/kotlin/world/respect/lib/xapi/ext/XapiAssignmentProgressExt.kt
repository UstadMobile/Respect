package world.respect.lib.xapi.ext

import world.respect.lib.xapi.composites.XapiActorAndAssignmentProgress
import world.respect.lib.xapi.composites.XapiAssignmentProgress

fun List<XapiAssignmentProgress>.averageScore(): XapiAssignmentProgress {
    return XapiAssignmentProgress(
        activityId = "",
        scoreScaled = mapNotNull { it.scoreScaled }.takeIf { it.isNotEmpty() }?.average()?.toFloat()
    )
}

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

fun XapiActorAndAssignmentProgress.isCompleted(): Boolean {
    return progress.all { it.completed == true }
}
