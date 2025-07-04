package world.respect.navigation

import androidx.navigation.NavHostController
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Wrapper that avoids accidental 'replay' of navigation commands.
 */
class RespectComposeNavController(
    private val navHostController: NavHostController,
) {

    @OptIn(ExperimentalTime::class)
    @Volatile
    private var lastNavCommandTime = Clock.System.now().toEpochMilliseconds()

    fun onCollectNavCommand(
        navCommand: NavCommand,
    ) {
        if(navCommand.timestamp <= lastNavCommandTime)
            return

        when (navCommand) {
            is NavCommand.Navigate -> {
                lastNavCommandTime = navCommand.timestamp
                navHostController.navigate(navCommand.destination)
            }
        }
    }

}