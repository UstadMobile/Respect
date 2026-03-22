package world.respect.datalayer.school.xapi.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import world.respect.lib.serializers.InstantAsISO8601

const val XAPI_RESULT_EXTENSION_PROGRESS = "https://w3id.org/xapi/cmi5/result/extensions/progress"

//This extension is wrong, but it is used by Articulate content
const val XAPI_RESULT_EXTENSION_PROGRESS_NON_HTTPS = "http://w3id.org/xapi/cmi5/result/extensions/progress"

val XAPI_PROGRESSED_EXTENSIONS = listOf(
    XAPI_RESULT_EXTENSION_PROGRESS, XAPI_RESULT_EXTENSION_PROGRESS_NON_HTTPS
)


/**
 * XapiStatement represents both a Statement and a SubStatement, therefor it implements the sealed
 * interface XapiStatementObject
 */
@Serializable
data class XapiStatement(
    val id: String? = null,
    val actor: XapiActor,
    val verb: XapiVerb,
    @SerialName("object")
    val `object`: XapiStatementObject,
    val result: XapiResult? = null,
    val context: XapiContext? = null,
    val timestamp: InstantAsISO8601? = null,
    val stored: InstantAsISO8601? = null,
    val authority: XapiActor? = null,
    val version: String? = null,
    val attachments: List<Attachment>? = null,
    override val objectType: XapiObjectType? = null,
): XapiStatementObject

