package world.respect.navigation

import world.respect.app.app.AppDestination
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
sealed class NavCommand(
    val timestamp: Long = Clock.System.now().toEpochMilliseconds(),
) {
    @OptIn(ExperimentalTime::class)
    class Navigate(
        val destination: AppDestination,
        timestamp: Long= Clock.System.now().toEpochMilliseconds(),
    ): NavCommand(timestamp)

}

