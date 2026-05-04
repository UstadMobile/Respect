package world.respect.lib.xapi.nanohttpd

import io.ktor.http.Url
import world.respect.lib.xapi.resources.XapiStatementsResource

fun interface XapiResourceProvider {

    operator fun invoke(endpoint: Url): XapiStatementsResource

}