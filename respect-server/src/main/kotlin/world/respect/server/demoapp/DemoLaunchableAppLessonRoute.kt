package world.respect.server.demoapp

import com.eygraber.uri.Uri
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import world.respect.lib.opds.model.LangMapStringValue
import world.respect.lib.opds.model.OpdsPublication
import world.respect.lib.opds.model.ReadiumContributorStringValue
import world.respect.lib.opds.model.ReadiumLink
import world.respect.lib.opds.model.ReadiumMetadata
import world.respect.libutil.ext.resolve
import world.respect.server.demoapp.ext.demoAppBaseUrl

fun Route.DemoLaunchableAppLessonRoute() {

    get("grade/{grade}/lesson/{lesson}/manifest.json") {
        val grade = call.parameters["grade"]!!
        val lessonNum = call.parameters["lesson"]!!

        val demoBase = call.demoAppBaseUrl()
        val lessonBase = demoBase.resolve("grade/$grade/lesson/$lessonNum/")

        call.respond(
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
                        href = demoBase.resolve("appmanifest.json").toString(),
                        type = "application/opds-publication+json"
                    )
                )
            )
        )
    }

}