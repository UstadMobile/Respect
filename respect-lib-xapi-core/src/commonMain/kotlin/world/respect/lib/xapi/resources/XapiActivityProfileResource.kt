package world.respect.lib.xapi.resources

import kotlin.time.Instant

/**
 * Xapi Activity Profile resource : implements the Document Resource for Activity Profiles as per:
 *
 *  https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Communication.md#27-activity-profile-resource
 *
 */
interface XapiActivityProfileResource : XapiDocumentResource<XapiActivityProfileResource.MultiDocParams, XapiActivityProfileResource.SingleDocumentParams> {

    data class MultiDocParams(
        val activityId: String,
        val since: Instant? = null,
    )

    data class SingleDocumentParams(
        val activityId: String,
        val profileId: String,
    )

}