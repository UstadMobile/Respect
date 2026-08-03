package world.respect.server.domain.school.demoapp

import io.ktor.http.Url
import world.respect.lib.xapi.rusticilaunch.model.TinCanXmlActivities
import world.respect.lib.xapi.rusticilaunch.model.TinCanXmlActivity
import world.respect.lib.xapi.rusticilaunch.model.TinCanXmlDocument
import world.respect.lib.xapi.rusticilaunch.model.TinCanXmlLaunch
import world.respect.libutil.ext.resolve
import world.respect.server.domain.school.demoapp.MakeDemoAppGradeCollectionsUseCase.Companion.GRADES_DIR_NAME
import world.respect.server.domain.school.demoapp.MakeDemoAppGradeCollectionsUseCase.Companion.LESSON_DIR_NAME
import world.respect.server.domain.school.demoapp.MakeDemoAppLessonHtmlUseCase.Companion.LESSON_HTML_FILENAME

class MakeDemoAppLessonTinCanXmlUseCase {

    operator fun invoke(
        baseUrl: Url,
        gradeNum: Int,
        lessonNum: Int,
    ): TinCanXmlDocument {
        return TinCanXmlDocument(
            activities = TinCanXmlActivities(
                listOf(
                    TinCanXmlActivity(
                        id = baseUrl.resolve(
                            "$GRADES_DIR_NAME/$gradeNum/$LESSON_DIR_NAME/$lessonNum/"
                        ).toString(),
                        type = "http://activitystrea.ms/schema/1.0/game",
                        name = "Grade $gradeNum Lesson $lessonNum",
                        launch = TinCanXmlLaunch(
                            lang = "en-US",
                            value = LESSON_HTML_FILENAME,
                        )
                    )
                )
            )
        )
    }


}