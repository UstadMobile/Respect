package world.respect.shared.navigation

import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
sealed class NavCommand(
    val timestamp: Long = Clock.System.now().toEpochMilliseconds(),
) {
    @OptIn(ExperimentalTime::class)
    class Navigate(
        val destination: RespectAppRoute,
        val clearBackStack: Boolean = false,
        timestamp: Long = Clock.System.now().toEpochMilliseconds(),
    ) : NavCommand(timestamp)
}
