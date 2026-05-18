package world.respect.lib.xapi.nanohttpd.ext

import fi.iki.elonen.NanoHTTPD
import io.ktor.http.Url
import io.ktor.http.protocolWithAuthority

/**
 * Add cross-origin headers.
 *
 * See https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Access-Control-Allow-Origin
 */
fun NanoHTTPD.Response.addXapiCORSHeaders(
    session: NanoHTTPD.IHTTPSession
) {
    val referrer = session.headers["referer"] ?: throw IllegalArgumentException("No referrer")
    val origin = Url(referrer).protocolWithAuthority

    addHeader("Access-Control-Allow-Origin", origin)

    session.headers["access-control-request-method"]?.also { requestMethods ->
        addHeader("Access-Control-Allow-Methods", requestMethods)
    }

    session.headers["access-control-request-headers"]?.also { requestHeaders ->
        addHeader("Access-Control-Allow-Headers", requestHeaders)
    }
}