package world.respect.datalayer.school.xapi.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class XapiContext(
    val instructor: XapiActor? = null,

    val registration: String? = null,

    val language: String? = null,

    val platform: String? = null,

    val revision: String? = null,

    val team: XapiActor? = null,

    val statement: XapiActivityStatementObject? = null,

    val contextActivities: XapiContextActivities? = null,

    val extensions: Map<String, JsonElement>? = null,
)
