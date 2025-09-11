package world.respect.server.util.ext

import com.ustadmobile.ihttp.ext.clientProtocolAndHost
import com.ustadmobile.ihttp.headers.asIHttpHeaders
import io.ktor.http.HttpHeaders
import io.ktor.http.Url
import io.ktor.http.fromHttpToGmtDate
import io.ktor.server.request.ApplicationRequest
import kotlin.time.Instant

val ApplicationRequest.virtualHost: Url
    get() = Url(headers.asIHttpHeaders().clientProtocolAndHost())

/**
 * The If-Modified-Since header is really only accurate to the nearest second, so we need to convert.
 * Because If-Modified-Since is ONLY used on request headers, it makes sense to put this as an
 * extension function here.
 */
fun ApplicationRequest.ifModifiedSinceSecs(): Long? {
    return call.request.headers[HttpHeaders.IfModifiedSince]?.fromHttpToGmtDate()?.timestamp?.let {
        it / 1_000
    }
}



/**
 * Shorthand to determine if the request has not been modified based on the If-Modified-Since header
 * from the request and when the underlying response data was stored (see README.md for discussion
 * of difference between last modified time and stored time).
 *
 * @param responseDataLastModified the time data was last modified in seconds since epoch
 */
fun ApplicationRequest.validateIfNotModifiedSince(
    responseDataLastModified: Instant,
) : Boolean {
    return ifModifiedSinceSecs()?.let { ifModifiedSinceSecs ->
        responseDataLastModified.epochSeconds <= ifModifiedSinceSecs
    } ?: false
}
