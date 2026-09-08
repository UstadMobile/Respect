package world.respect.lib.xapi.resources

import io.ktor.http.ParametersBuilder
import io.ktor.util.StringValues
import world.respect.lib.xapi.exceptions.XapiException
import world.respect.libutil.ext.appendIfNotNull
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
    ) {
        fun toParameters(): StringValues {
            return ParametersBuilder().also { parameters ->
                parameters.append("activityId", activityId)
                parameters.appendIfNotNull("since", since?.toString())
            }.build()
        }

        companion object {
            fun fromParams(params: StringValues): MultiDocParams {
                return MultiDocParams(
                    activityId = params["activityId"] ?: throw XapiException(400, "activityId is required"),
                    since = params["since"]?.let { Instant.parse(it) },
                )
            }
        }
    }

    data class SingleDocumentParams(
        val activityId: String,
        val profileId: String,
    ) {
        fun toParameters(): StringValues {
            return ParametersBuilder().also { parameters ->
                parameters.append("activityId", activityId)
                parameters.append("profileId", profileId)
            }.build()
        }

        companion object {
            fun fromParams(params: StringValues): SingleDocumentParams {
                return SingleDocumentParams(
                    activityId = params["activityId"] ?: throw XapiException(400, "activityId is required"),
                    profileId = params["profileId"] ?: throw XapiException(400, "profileId is required"),
                )
            }
        }
    }

    companion object {

        const val ENDPOINT_NAME = "profile"

    }

}