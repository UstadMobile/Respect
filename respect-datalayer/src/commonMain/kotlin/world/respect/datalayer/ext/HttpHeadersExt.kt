package world.respect.datalayer.ext

import io.ktor.http.HttpMessage
import io.ktor.http.lastModified
import world.respect.datalayer.DataLayerHeaders
import kotlin.time.Instant

fun HttpMessage.lastModifiedAsLong(): Long {
    return lastModified()?.time ?: -1
}

fun HttpMessage.consistentThrough(): Instant? {
    return headers[DataLayerHeaders.XConsistentThrough]?.let {
        Instant.parse(it)
    }
}

