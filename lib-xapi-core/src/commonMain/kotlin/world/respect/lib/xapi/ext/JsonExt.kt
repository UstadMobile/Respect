package world.respect.lib.xapi.ext

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import world.respect.lib.xapi.model.XapiDocument
import world.respect.lib.xapi.model.XapiDocumentByteArrayImpl
import kotlin.time.Clock
import kotlin.time.Instant

fun <T: Any> Json.encodeToXapiDocument(
    serializer: SerializationStrategy<T>,
    value: T,
    updated: Instant = Clock.System.now(),
): XapiDocument {
    return XapiDocumentByteArrayImpl(
        type = "application/json",
        updated = updated,
        contents = encodeToString(serializer, value).encodeToByteArray()
    )
}

suspend fun <T: Any> Json.decodeFromXapiDocument(
    deserializer: DeserializationStrategy<T>,
    document: XapiDocument,
) : T {
    return decodeFromString(
        deserializer = deserializer, document.contentsAsByteArray().decodeToString()
    )
}
