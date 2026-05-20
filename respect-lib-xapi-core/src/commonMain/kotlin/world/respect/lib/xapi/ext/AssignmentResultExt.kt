package world.respect.lib.xapi.ext

import world.respect.lib.xapi.model.AssignmentResult

/**
 * Calculates the display percentage for an assignment result.
 * Logic: uses 'progress' if available, otherwise scales 'scoreScaled' to 100.
 */
fun AssignmentResult.calculatePercentage(): Int? {
    return progress ?: scoreScaled?.let { (it * 100).toInt() }
}
