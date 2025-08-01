package world.respect.datalayer.ext

import io.ktor.http.HttpMessage
import io.ktor.http.lastModified

fun HttpMessage.lastModifiedAsLong(): Long {
    return lastModified()?.time ?: -1
}
