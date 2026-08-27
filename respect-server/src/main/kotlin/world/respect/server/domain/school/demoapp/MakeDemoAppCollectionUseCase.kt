package world.respect.server.domain.school.demoapp

import io.ktor.http.Url
import org.openeel.demo.demolaunchableappserver.DemoConstants
import world.respect.lib.opds.model.OpdsFeed
import world.respect.lib.opds.model.OpdsFeedMetadata
import world.respect.lib.opds.model.ReadiumLink
import world.respect.libutil.ext.resolve
import world.respect.server.domain.school.demoapp.MakeDemoAppManifestUseCase.Companion.APP_MANIFEST_ICON_NAME

class MakeDemoAppCollectionUseCase {

    operator fun invoke(
        baseUrl: Url
    ): OpdsFeed {
        return OpdsFeed(
            metadata = OpdsFeedMetadata(
                title = "Demo Launchable App Lessons",
                description = "List of app lessons",
            ),
            links = listOf(
                ReadiumLink(
                    href = baseUrl.resolve("default-collection.json").toString(),
                    rel = listOf("self"),
                    type = "application/json"
                ),
                ReadiumLink(
                    rel = listOf("icon"),
                    href = baseUrl.resolve(APP_MANIFEST_ICON_NAME).toString(),
                    type = "image/png",
                ),
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
    }

    companion object {

        const val DEFAULT_COLLECTION_NAME = "default-collection.json"

        const val GRADE_ICON_NAME = "grade.jpg"

    }

}