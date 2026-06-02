package world.respect.shared.domain.launchapp

import io.ktor.http.Url
import world.respect.lib.opds.model.OpdsPublication

/**
 * Interface to launch a RESPECT compatible app.
 */
interface LaunchAppUseCase {

    /**
     * @param publicationUrl The URL from which the publication is loaded
     * @param publication the publication to open: this can be a learning unit or compatible app.
     * @param assignmentActivityId if a learning unit id is being launched as part of an assignment,
     *        the assignmentActivityId. This will be used by the embedded xAPI server to modify
     *        statements received to follow the assignment recipe (add assignmentActivityId to
     *        contextActivities).
     */
    data class LaunchRequest(
        val publicationUrl: Url,
        val publication: OpdsPublication,
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