package world.respect.shared.domain.launchapp

import io.ktor.http.Url
import world.respect.lib.opds.model.OpdsPublication

/**
 * Interface to launch a RESPECT compatible app.
 */
interface LaunchAppUseCase {

    /**
     * @param app the Respect compatible app
     * @param learningUnitId the url of the learning unit to launch (if null, launch the default
     *        url for the given app)
     * @param assignmentActivityId if a learning unit id is being launched as part of an assignment,
     *        the assignmentActivityId. This will be used by the embedded xAPI server to modify
     *        statements received to follow the assignment recipe (add assignmentActivityId to
     *        contextActivities).
     */
    data class LaunchRequest(
        val app: OpdsPublication,
        val learningUnitId: Url?,
        val assignmentActivityId: String? = null,
    )

    suspend operator fun invoke(
        request: LaunchRequest
    )

    companion object {

        //As per integration guide
        const val RESPECT_LAUNCH_VERSION_PARAM_NAME = "respectLaunchVersion"

        const val RESPECT_LAUNCH_VERSION_VALUE = "1"

    }

}