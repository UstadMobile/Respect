package world.respect.lib.xapi.resources

import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.xapi.model.XapiDocument
import kotlin.time.Instant

/**
 * Xapi Activity Profile resource
 *
 * As per : https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Communication.md#27-activity-profile-resource
 *
 */
interface XapiActivityProfileResource {

    data class GetActivityProfilesResourceParams(
        val activityId: String,
        val since: Instant? = null,
    )

    data class GetActivityProfileResourceParams(
        val activityId: String,
        val profileId: String,
    )

    /**
     *
     */
    suspend fun getProfiles(params: GetActivityProfilesResourceParams): DataLoadState<List<String>>

    /**
     *
     */
    suspend fun getProfile(
        params: GetActivityProfileResourceParams,
    ): DataLoadState<XapiDocument>

}