package world.respect.server.domain.school.demoapp

import io.ktor.http.Url
import io.ktor.http.hostWithPortIfSpecified
import org.openeel.demo.demolaunchableappserver.DemoConstants
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
        demoAppPackage: String = DEMO_APP_PACKAGE,
        useIntentUrl: Boolean = gradeNum == DemoConstants.APP_ONLY_GRADE,
        titleFn: (Int, Int) -> String = MakeDemoAppLearningUnitManifestUseCase.LEARNING_UNIT_TITLE_FN,
    ): TinCanXmlDocument {
        val lessonPath = "$GRADES_DIR_NAME/$gradeNum/$LEARNING_UNITS_DIR_NAME/$lessonNum/"

        return TinCanXmlDocument(
            activities = TinCanXmlActivities(
                listOf(
                    TinCanXmlActivity(
                        id = baseUrl.resolve(lessonPath).toString(),
                        type = "http://activitystrea.ms/schema/1.0/game",
                        name = titleFn(gradeNum, lessonNum),
                        launch = TinCanXmlLaunch(
                            lang = "en-US",
                            value = if(useIntentUrl) {
                                "intent://${baseUrl.hostWithPortIfSpecified}/$lessonPath$LEARNING_UNIT_HTML_FILENAME#Intent;scheme=https;category=android.intent.category.BROWSABLE;package=$demoAppPackage;end"
                            }else {
                                LEARNING_UNIT_HTML_FILENAME
                            }
                        )
                    )
                )
            )
        )
    }

    companion object {

        const val DEMO_APP_PACKAGE = "org.openeel.demolaunchableapp"
    }


}