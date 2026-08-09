package world.respect.server.domain.school.demoapp

import io.ktor.http.Url
import world.respect.lib.xapi.rusticilaunch.model.TinCanXmlActivities
import world.respect.lib.xapi.rusticilaunch.model.TinCanXmlActivity
import world.respect.lib.xapi.rusticilaunch.model.TinCanXmlDocument
import world.respect.lib.xapi.rusticilaunch.model.TinCanXmlLaunch
import world.respect.libutil.ext.resolve
import world.respect.server.domain.school.demoapp.MakeDemoAppGradeCollectionsUseCase.Companion.GRADES_DIR_NAME
import world.respect.server.domain.school.demoapp.MakeDemoAppGradeCollectionsUseCase.Companion.LEARNING_UNITS_DIR_NAME
import world.respect.server.domain.school.demoapp.MakeDemoAppLearningUnitHtmlUseCase.Companion.LEARNING_UNIT_HTML_FILENAME

class MakeDemoAppLearningUnitTinCanXmlUseCase {

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
                            "$GRADES_DIR_NAME/$gradeNum/$LEARNING_UNITS_DIR_NAME/$lessonNum/"
                        ).toString(),
                        type = "http://activitystrea.ms/schema/1.0/game",
                        name = "Grade $gradeNum Lesson $lessonNum",
                        launch = TinCanXmlLaunch(
                            lang = "en-US",
                            value = LEARNING_UNIT_HTML_FILENAME,
                        )
                    )
                )
            )
        )
    }


}