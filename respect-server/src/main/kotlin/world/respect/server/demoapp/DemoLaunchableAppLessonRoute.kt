package world.respect.server.demoapp

import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import org.koin.ktor.ext.getKoin
import world.respect.server.demoapp.ext.demoAppBaseUrl
import world.respect.server.domain.school.demoapp.MakeDemoAppGradeCollectionsUseCase.Companion.GRADES_DIR_NAME
import world.respect.server.domain.school.demoapp.MakeDemoAppGradeCollectionsUseCase.Companion.LESSON_DIR_NAME
import world.respect.server.domain.school.demoapp.MakeDemoAppLessonManifestUseCase
import world.respect.server.domain.school.demoapp.MakeDemoAppLessonManifestUseCase.Companion.LESSON_MANIFEST_FILENAME

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

}