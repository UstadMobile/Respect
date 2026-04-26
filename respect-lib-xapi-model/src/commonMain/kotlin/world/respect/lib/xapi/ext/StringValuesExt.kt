package world.respect.lib.xapi.ext

import io.ktor.util.StringValues
import kotlin.uuid.Uuid

fun StringValues.getUuidOrNull(
    name: String
): Uuid? {
    return get(name)?.let { Uuid.parse(it) }
}
