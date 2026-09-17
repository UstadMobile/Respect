package world.respect.lib.dataloadstate.ext

import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.fromHttpToGmtDate
import world.respect.lib.dataloadstate.ETagAndLastModified
import world.respect.lib.dataloadstate.datetime.toInstant
import kotlin.time.Instant

/**
 * Simple short function to check if an incoming request has cache validation parameters.
 */
fun Headers.hasIfNotModifiedHeaders() : Boolean {
    return this[HttpHeaders.IfNoneMatch] != null || this[HttpHeaders.IfModifiedSince] != null
}

/**
 * Check if the response for a given set of request headers
 *
 * @receiver Headers request headers
 */
fun Headers.isNotModified(
    responseETagAndLastModified: ETagAndLastModified,
): Boolean {
    /**
     * ETag takes precendence:
     */
    val ifNoneMatch = this[HttpHeaders.IfNoneMatch]
    val ifModifiedSince = this[HttpHeaders.IfModifiedSince]

    return if (ifNoneMatch != null) {
        responseETagAndLastModified.etag != null && responseETagAndLastModified.etag == ifNoneMatch
    } else if (ifModifiedSince != null) {
        responseETagAndLastModified.lastModified?.let {
            it <= (ifModifiedSince.fromHttpToGmtDate().toInstant())
        } == true
    } else {
        false
    }
}
