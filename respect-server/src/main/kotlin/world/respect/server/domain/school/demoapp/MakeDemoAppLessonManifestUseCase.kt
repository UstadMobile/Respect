package world.respect.server.domain.school.demoapp

import com.eygraber.uri.Uri
import io.ktor.http.Url
import world.respect.lib.opds.model.LangMapStringValue
import world.respect.lib.opds.model.OpdsPublication
import world.respect.lib.opds.model.ReadiumContributorStringValue
import world.respect.lib.opds.model.ReadiumLink
import world.respect.lib.opds.model.ReadiumMetadata
import world.respect.libutil.ext.resolve
import world.respect.server.domain.school.demoapp.MakeDemoAppGradeCollectionsUseCase.Companion.GRADES_DIR_NAME
import world.respect.server.domain.school.demoapp.MakeDemoAppGradeCollectionsUseCase.Companion.LESSON_DIR_NAME
import world.respect.server.domain.school.demoapp.MakeDemoAppGradeCollectionsUseCase.Companion.LESSON_ICON_NAME
import world.respect.server.domain.school.demoapp.MakeDemoAppLessonHtmlUseCase.Companion.LESSON_HTML_FILENAME
import world.respect.server.domain.school.demoapp.MakeDemoAppLessonHtmlUseCase.Companion.LESSON_JS_FILENAME
import world.respect.server.domain.school.demoapp.MakeDemoAppLessonHtmlUseCase.Companion.XAPI_MODULE_FILENAME
import world.respect.server.domain.school.demoapp.MakeDemoAppManifestUseCase.Companion.APP_MANIFEST_FILENAME

class MakeDemoAppLessonManifestUseCase {

    operator fun invoke(
        demoBase: Url,
        grade: Int,
        lessonNum: Int,
    ): OpdsPublication {
        val lessonBase = demoBase.resolve("$GRADES_DIR_NAME/$grade/$LESSON_DIR_NAME/$lessonNum/")

        return OpdsPublication(
            metadata = ReadiumMetadata(
                title = LangMapStringValue("Lesson $lessonNum - Grade $grade"),
                type = Uri.parse("http://schema.org/Game"),
                author = listOf(
                    ReadiumContributorStringValue("Mullah Nasruddin")
                ),
                identifier = Uri.parse(lessonBase.toString())
            ),
            images = listOf(
                ReadiumLink(
                    href = demoBase.resolve("static/$LESSON_ICON_NAME").toString(),
                    type = "image/png"
                )
            ),
            links = listOf(
                ReadiumLink(
                    rel = listOf("self"),
                    href = lessonBase.resolve(LESSON_MANIFEST_FILENAME).toString(),
                    type = "application/opds-publication+json"
                ),
                ReadiumLink(
                    rel = listOf("https://id.openeel.org/rel/tincanxml"),
                    href = lessonBase.resolve("tincan.xml").toString(),
                    type = "application/xml"
                ),
                ReadiumLink(
                    rel = listOf("https://id.openeel.org/rel/launchable-app"),
                    href = demoBase.resolve(APP_MANIFEST_FILENAME).toString(),
                    type = "application/opds-publication+json"
                )
            ),
            resources = listOf(
                ReadiumLink(href = lessonBase.resolve(LESSON_HTML_FILENAME).toString()),
                ReadiumLink(href = demoBase.resolve("static/$LESSON_JS_FILENAME").toString()),
                ReadiumLink(href = demoBase.resolve("static/$XAPI_MODULE_FILENAME").toString()),
            ),
        )
    }


    companion object {

        const val LESSON_MANIFEST_FILENAME = "manifest.json"

    }
}