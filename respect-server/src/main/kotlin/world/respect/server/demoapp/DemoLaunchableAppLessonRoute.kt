package world.respect.server.demoapp

import io.ktor.http.ContentType
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import nl.adaptivity.xmlutil.serialization.XML
import org.koin.ktor.ext.getKoin
import world.respect.lib.xapi.rusticilaunch.model.TinCanXmlDocument
import world.respect.server.demoapp.ext.demoAppBaseUrl
import world.respect.server.domain.school.demoapp.MakeDemoAppGradeCollectionsUseCase.Companion.GRADES_DIR_NAME
import world.respect.server.domain.school.demoapp.MakeDemoAppGradeCollectionsUseCase.Companion.LESSON_DIR_NAME
import world.respect.server.domain.school.demoapp.MakeDemoAppLessonManifestUseCase
import world.respect.server.domain.school.demoapp.MakeDemoAppLessonManifestUseCase.Companion.LESSON_MANIFEST_FILENAME
import world.respect.server.domain.school.demoapp.MakeDemoAppLessonTinCanXmlUseCase

fun Route.DemoLaunchableAppLessonRoute() {
    val koin = getKoin()

    get("$GRADES_DIR_NAME/{grade}/$LESSON_DIR_NAME/{lesson}/$LESSON_MANIFEST_FILENAME") {
        call.respond(
            koin.get<MakeDemoAppLessonManifestUseCase>().invoke(
                demoBase = call.demoAppBaseUrl(),
                grade = call.parameters["grade"]!!.toInt(),
                lessonNum = call.parameters["lesson"]!!.toInt(),
            )
        )
    }

    get("$GRADES_DIR_NAME/{grade}/$LESSON_DIR_NAME/{lesson}/tincan.xml") {
        val xml = koin.get<XML>()

        call.respondText(
            contentType = ContentType.Application.Xml,
            text = xml.encodeToString(
                TinCanXmlDocument.serializer(),
                koin.get<MakeDemoAppLessonTinCanXmlUseCase>().invoke(
                    baseUrl = call.demoAppBaseUrl(),
                    gradeNum = call.parameters["grade"]!!.toInt(),
                    lessonNum = call.parameters["lesson"]!!.toInt(),
                )
            )
        )
    }

}