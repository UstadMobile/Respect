package world.respect.lib.xapi.ext

import io.ktor.http.Url
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import world.respect.lib.xapi.OpenEelXapiConstants
import world.respect.lib.xapi.model.XapiActivityDefinition
import kotlin.time.Instant

/**
 * Get the web pub manifest url as a string via the extension if present
 */
fun XapiActivityDefinition.webPubManifestOrNull(): String? {
    val jsonPrimitive = extensions?.get(
        OpenEelXapiConstants.ACTIVITY_EXTENSION_WEBPUB_MANIFEST_LINK
    ) as? JsonPrimitive
    return jsonPrimitive?.contentOrNull
}

/**
 * Get the web pub manifest url as a url via the extension if present and a valid url
 */
fun XapiActivityDefinition.webPubManifestAsUrlOrNull(): Url? {
    return webPubManifestOrNull()?.runCatching { Url(this) }?.getOrNull()
}

/**
 * Get the deadline as a string via the extension if present
 */
fun XapiActivityDefinition.extensionDeadlineAsInstantOrNull(): Instant? {
    return extensions?.get(OpenEelXapiConstants.ACTIVITY_EXTENSION_DEADLINE)
        ?.takeIf { it is JsonPrimitive }?.jsonPrimitive?.contentOrNull?.let {
            try { Instant.parse(it) }
            catch (_: Throwable) { null }
        }
}

