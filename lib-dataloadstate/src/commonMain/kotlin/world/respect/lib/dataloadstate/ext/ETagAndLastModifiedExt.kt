package world.respect.lib.dataloadstate.ext

import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.headers
import io.ktor.http.toHttpDate
import world.respect.lib.dataloadstate.ETagAndLastModified
import world.respect.lib.dataloadstate.datetime.toGMTDate

/**
 * Create response headers that contain the ETag and Last-Modified header if the receiver's
 * etag and last-modified are non-null respectively.
 *
 * @receiver [ETagAndLastModified]
 * @return headers with ETag and Last-Modified set if the respective properties of the
 *         receiver are non-null.
 */
fun ETagAndLastModified.toResponseHeaders() : Headers {
    return headers {
        etag?.also {
            set(HttpHeaders.ETag, it)
        }

        lastModified?.also {
            set(HttpHeaders.LastModified, it.toGMTDate().toHttpDate())
        }
    }
}


/**
 * Validate a new ETagAndLastModified against a previously received ETagAndLastModified.
 *
 * @receiver previously stored/received ETagAndLastModified
 * @param other ETagAndLastModified to validate against
 *
 * @return true if the receiver validates against the other ETagAndLastModified
 *
 */
fun ETagAndLastModified.isStillValid(
    other: ETagAndLastModified
) : Boolean{
    return if (etag != null) {
        other.etag == etag
    } else if (lastModified != null) {
        other.lastModified?.let { newLastMod ->
            newLastMod <= lastModified
        } == true
    } else {
        false
    }
}
