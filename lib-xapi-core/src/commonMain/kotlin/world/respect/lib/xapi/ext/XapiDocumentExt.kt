package world.respect.lib.xapi.ext

import io.ktor.http.ContentType
import world.respect.lib.xapi.model.XapiDocument

/**
 * Simple shorthand method to check if the receiver [XapiDocument] is Json.
 *
 * @receiver a XapiDocument
 * @return true if the receiver [XapiDocument] is Json, false otherwise
 */
fun XapiDocument.isJson() : Boolean = ContentType.Application.Json.match(type)
