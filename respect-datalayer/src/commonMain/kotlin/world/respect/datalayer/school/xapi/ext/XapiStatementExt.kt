package world.respect.datalayer.school.xapi.ext

import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import world.respect.datalayer.school.xapi.model.XAPI_PROGRESSED_EXTENSIONS
import world.respect.datalayer.school.xapi.model.XapiStatement

val XapiStatement.resultProgressExtension: Int?
    get() = result?.extensions?.let { extensions ->
        XAPI_PROGRESSED_EXTENSIONS.firstNotNullOfOrNull { extensionKey ->
            extensions.get(extensionKey)?.jsonPrimitive?.intOrNull
        }
    }


/**
 * Shorthand to determine if this statement contains completion or progress data.
 */
fun XapiStatement.isCompletionOrProgress(): Boolean {
    return result?.completion != null || resultProgressExtension != null
}
