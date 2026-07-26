package world.respect.server.demoapp.ext

import io.ktor.http.Url
import io.ktor.server.routing.RoutingCall


const val DEFAULT_DOMAIN = "https://demo.openeel.org/"

fun RoutingCall.demoAppBaseUrl(): Url {
    return Url(
        application.environment.config.propertyOrNull(
            "ktor.demobaseurl"
        )?.getString() ?: DEFAULT_DOMAIN
    )
}
