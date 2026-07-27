package org.openeel.demo.demolaunchableappserver

import com.eygraber.uri.Uri
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import world.respect.lib.opds.model.LangMapStringValue
import world.respect.lib.opds.model.OpdsFeed
import world.respect.lib.opds.model.OpdsFeedMetadata
import world.respect.lib.opds.model.OpdsPublication
import world.respect.lib.opds.model.ReadiumContributorStringValue
import world.respect.lib.opds.model.ReadiumLink
import world.respect.lib.opds.model.ReadiumMetadata
import world.respect.libutil.ext.resolve
import world.respect.server.demoapp.ext.demoAppBaseUrl


fun Route.DemoLaunchableAppCollectionsRoute() {
    get("default-collection.json") {
        val baseUrl = call.demoAppBaseUrl()

        call.respond(
            OpdsFeed(
                metadata = OpdsFeedMetadata(
                    title = "Demo Launchable App Lessons"
                ),
                links = listOf(
                    ReadiumLink(
                        href = baseUrl.resolve("default-collection.json").toString(),
                        rel = listOf("self"),
                        type = "application/json"
                    )
                ),
                navigation = (1..DemoConstants.NUM_GRADES).map {
                    ReadiumLink(
                        href = baseUrl.resolve("grade/$it/index.json").toString(),
                        title = "Grade $it",
                        type = "application/json",
                        alternate = listOf(
                            ReadiumLink(
                                rel = listOf("icon"),
                                href = baseUrl.resolve("static/grade.jpg").toString(),
                                type = "image/png",
                            ),
                        )
                    )
                }
            )
        )
    }

    get("grade/{grade}/index.json") {
        val baseUrl = call.demoAppBaseUrl()
        val grade = call.parameters["grade"]!!

        call.respond(
            OpdsFeed(
                metadata = OpdsFeedMetadata(
                    title = "Grade $grade"
                ),
                links = listOf(
                    ReadiumLink(
                        href = baseUrl.resolve("grade/$grade/index.json").toString(),
                        rel = listOf("self"),
                        type = "application/json"
                    )
                ),
                publications = (1..DemoConstants.NUM_LESSONS).map { lessonNum ->
                    val lessonBase = baseUrl.resolve("grade/$grade/lesson/$lessonNum/")
                    OpdsPublication(
                        metadata = ReadiumMetadata(
                            title = LangMapStringValue("Lesson $lessonNum - Grade $grade"),
                            type = Uri.parse("http://schema.org/Game"),
                            author = listOf(
                                ReadiumContributorStringValue("Mullah Nasruddin")
                            ),
                            identifier = Uri.parse(lessonBase.toString())
                        ),
                        links = listOf(
                            ReadiumLink(
                                rel = listOf("self"),
                                href = lessonBase.resolve("manifest.json").toString(),
                                type = "application/opds-publication+json"
                            ),
                            ReadiumLink(
                                rel = listOf("https://id.openeel.org/rel/tincanxml"),
                                href = lessonBase.resolve("tincan.xml").toString(),
                                type = "application/xml"
                            ),
                            ReadiumLink(
                                rel = listOf("https://id.openeel.org/rel/launchable-app"),
                                href = baseUrl.resolve("appmanifest.json").toString(),
                                type = "application/opds-publication+json"
                            )
                        ),
                        images = listOf(
                            ReadiumLink(
                                href = baseUrl.resolve("static/books.png").toString(),
                                type = "image/png"
                            )
                        ),
                    )
                }
            )
        )

    }

}