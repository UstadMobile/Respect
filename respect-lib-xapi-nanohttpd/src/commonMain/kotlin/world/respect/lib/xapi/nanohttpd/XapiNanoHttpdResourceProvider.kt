package world.respect.lib.xapi.nanohttpd

import io.ktor.http.Url
import world.respect.lib.xapi.resources.XapiStatementsResource

/**
 * Simple interface that can be implemented to provide a XapiResource to the embedded server based on
 * an endpoint url and authentication.
 */
fun interface XapiNanoHttpdResourceProvider {

    operator fun invoke(
        endpoint: Url,
        authentication: String,
    ): XapiStatementsResource

}