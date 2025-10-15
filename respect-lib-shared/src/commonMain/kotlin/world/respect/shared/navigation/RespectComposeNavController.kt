package world.respect.shared.navigation

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
                if (navCommand.clearBackStack) {
                    navHostController.navigate(navCommand.destination) {
                        popUpTo(0) { inclusive = true }
                    }
                } else {
                    navHostController.navigate(navCommand.destination) {
                        navCommand.popUpTo?.also {
                            popUpTo(it) { inclusive = navCommand.popUpToInclusive }
                        }
                    }
                }
            }

            is NavCommand.Pop -> {
                lastNavCommandTime = navCommand.timestamp
                navHostController.popBackStack(
                    navCommand.destination, navCommand.inclusive
                )
            }

            is NavCommand.PopUp -> {
                lastNavCommandTime = navCommand.timestamp
                navHostController.popBackStack()
            }
        }
    }

}