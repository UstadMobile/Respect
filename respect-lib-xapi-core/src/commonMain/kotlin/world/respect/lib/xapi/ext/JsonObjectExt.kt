package world.respect.lib.xapi.ext

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlin.collections.component1
import kotlin.collections.component2

fun JsonObjectBuilder.putAllExcept(
    other: JsonObject,
    exceptKeys: List<String>
) {
    other.entries.forEach { (key, value) ->
        if(key !in exceptKeys) {
            put(key, value)
        }
    }
}

fun JsonObjectBuilder.putAll(
    other: JsonObject
) {
    other.entries.forEach {
        put(it.key, it.value)
    }
}

