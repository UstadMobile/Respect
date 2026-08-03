package world.respect.server.domain.school.demoapp

import io.ktor.http.Url
import kotlinx.serialization.json.Json
import nl.adaptivity.xmlutil.serialization.XML
import org.openeel.demo.demolaunchableappserver.DemoConstants
import world.respect.lib.xapi.rusticilaunch.model.TinCanXmlDocument
import world.respect.server.domain.school.demoapp.MakeDemoAppCollectionUseCase.Companion.GRADE_ICON_NAME
import world.respect.server.domain.school.demoapp.MakeDemoAppGradeCollectionsUseCase.Companion.GRADES_DIR_NAME
import world.respect.server.domain.school.demoapp.MakeDemoAppGradeCollectionsUseCase.Companion.LESSON_DIR_NAME
import world.respect.server.domain.school.demoapp.MakeDemoAppGradeCollectionsUseCase.Companion.LESSON_ICON_NAME
import world.respect.server.domain.school.demoapp.MakeDemoAppManifestUseCase.Companion.APP_MANIFEST_FILENAME
import java.io.File

class SaveDemoAppToStaticFilesUseCase(
    private val makeDemoAppManifestUseCase: MakeDemoAppManifestUseCase,
    private val makeDemoAppCollectionUseCase: MakeDemoAppCollectionUseCase,
    private val makeDemoAppGradeCollectionsUseCase: MakeDemoAppGradeCollectionsUseCase,
    private val makeDemoAppLessonManifestUseCase: MakeDemoAppLessonManifestUseCase,
    private val makeDemoAppLessonTinCanXmlUseCase: MakeDemoAppLessonTinCanXmlUseCase,
    private val json: Json,
    private val xml: XML,
) {

    operator fun invoke(
        destDir: File,
        baseUrl: Url,
    ) {
        destDir.takeIf { !it.exists() }?.mkdirs()
        val staticDir = File(destDir, "static")
        staticDir.mkdirs()

        File(destDir, APP_MANIFEST_FILENAME).writeText(
            json.encodeToString(makeDemoAppManifestUseCase(baseUrl))
        )

        File(destDir, MakeDemoAppManifestUseCase.APP_MANIFEST_ICON_NAME).writeBytes(
            this::class.java.getResourceAsStream("/demoapp/app_icon.png")!!.readBytes()
        )

        File(destDir, MakeDemoAppCollectionUseCase.DEFAULT_COLLECTION_NAME).writeText(
            json.encodeToString(makeDemoAppCollectionUseCase(baseUrl))
        )

        listOf(GRADE_ICON_NAME, LESSON_ICON_NAME).forEach { resourceName ->
            File(staticDir, resourceName).writeBytes(
                this::class.java.getResourceAsStream("/demoapp/$resourceName")!!.readBytes()
            )
        }

        val gradesDir = File(destDir, GRADES_DIR_NAME).also { it.mkdirs() }
        (1..DemoConstants.NUM_LESSONS).forEach { gradeNum ->
            val gradeDir = File(gradesDir, gradeNum.toString()).also {
                it.mkdirs()
            }

            File(gradeDir, MakeDemoAppGradeCollectionsUseCase.COLLECTION_FILE_NAME).writeText(
                json.encodeToString(
                    makeDemoAppGradeCollectionsUseCase(
                        baseUrl = baseUrl,
                        gradeNum = gradeNum,
                    )
                )
            )
            val lessonsDir = File(gradeDir, LESSON_DIR_NAME).also { it.mkdirs() }
            (1..DemoConstants.NUM_LESSONS).forEach { lessonNum ->
                val lessonDir = File(lessonsDir, lessonNum.toString()).also { it.mkdirs() }

                File(
                    lessonDir, MakeDemoAppLessonManifestUseCase.LESSON_MANIFEST_FILENAME
                ).writeText(
                    json.encodeToString(
                        makeDemoAppLessonManifestUseCase(
                            demoBase = baseUrl,
                            grade = gradeNum,
                            lessonNum = lessonNum,
                        )
                    )
                )

                File(lessonDir, "tincan.xml").writeText(
                    xml.encodeToString(
                        TinCanXmlDocument.serializer(),
                        makeDemoAppLessonTinCanXmlUseCase(
                            baseUrl = baseUrl,
                            gradeNum = gradeNum,
                            lessonNum = lessonNum,
                        )
                    )
                )
            }
        }

    }
}