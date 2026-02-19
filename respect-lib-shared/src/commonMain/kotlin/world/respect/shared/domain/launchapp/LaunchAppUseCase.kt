package world.respect.shared.domain.launchapp

import io.ktor.http.Url
import world.respect.lib.opds.model.OpdsPublication
import world.respect.shared.navigation.NavCommand

/**
 * Interface to launch a RESPECT compatible app.
 */
interface LaunchAppUseCase {

    operator fun invoke(
        app: OpdsPublication,
        learningUnitId: Url?,
        navigateFn: (NavCommand) -> Unit,
    )

    companion object {

        //As per integration guide
        const val RESPECT_LAUNCH_VERSION_PARAM_NAME = "respectLaunchVersion"

        const val RESPECT_LAUNCH_VERSION_VALUE = "1"

    }

}