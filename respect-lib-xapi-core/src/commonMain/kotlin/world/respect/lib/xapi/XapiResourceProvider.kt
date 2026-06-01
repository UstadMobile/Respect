package world.respect.lib.xapi

import world.respect.lib.xapi.resources.XapiResource

/**
 * Certain components (e.g. an HttpServer, IPC server service, etc) need to be able to lookup a
 * XapiResource based on the endpoint and authentication.
 */
fun interface XapiResourceProvider {

    operator fun invoke(
        endpoint: String,
        authentication: String?,
    ): XapiResource

}