package org.openeel.demo.demolaunchableappserver

import com.eygraber.uri.Uri
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import world.respect.lib.opds.model.LangMapStringValue
import world.respect.lib.opds.model.OpdsPublication
import world.respect.lib.opds.model.ReadiumContributorObject
import world.respect.lib.opds.model.ReadiumLink
import world.respect.lib.opds.model.ReadiumMetadata
import world.respect.libutil.ext.resolve
import world.respect.server.demoapp.ext.demoAppBaseUrl

fun Route.DemoLaunchableAppManifestRoute() {
    get("appmanifest.json") {
        val domain = call.demoAppBaseUrl()

        call.respond(
            OpdsPublication(
                metadata = ReadiumMetadata(
                    title = LangMapStringValue("Demo Launchable App"),
                    author = listOf(
                        ReadiumContributorObject(
                            name = "UstadMobile FZ-LLC",
                            links = listOf(
                                ReadiumLink(
                                    href = "https://www.ustadmobile.com/"
                                )
                            )
                        )
                    ),
                    identifier = Uri.parse("https://demo.openeel.org/app"),
                    language = listOf("en"),
                    modified = "2025-09-29T17:00:00Z"
                ),
                links = listOf(
                    ReadiumLink(
                        href = domain.resolve("appmanifest.json").toString(),
                        rel = listOf("self"),
                        type = "application/opds-publication+json"
                    ),
                    ReadiumLink(
                        rel = listOf("collection"),
                        type = "application/opds+json",
                        href = domain.resolve("default-collection.json").toString()
                    ),
                    ReadiumLink(
                        rel = listOf("https://id.openeel.org/rel/app-launch-uri"),
                        href = domain.toString(),
                    ),
                    ReadiumLink(
                        rel = listOf("https://id.openeel.org/rel/appstore-android"),
                        href = "https://play.google.com/store/apps/details?id=org.openeel.demo",
                        title = "Get it on Google Play",
                    ),
                    ReadiumLink(
                        rel = listOf("terms-of-service"),
                        href = domain.resolve("terms-privacy.html").toString(),
                    ),
                    ReadiumLink(
                        rel = listOf("license"),
                        href = "http://opensource.org/licenses/MIT"
                    )
                ),
                images = listOf(
                    ReadiumLink(
                        href = domain.resolve("static/app-icon.png").toString(),
                        type = "image/png",
                    )
                )
            )
        )
    }

}
