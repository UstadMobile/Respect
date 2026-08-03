package world.respect.server.domain.school.demoapp

import io.ktor.http.Url
import kotlinx.html.HTML
import kotlinx.html.InputType
import kotlinx.html.body
import kotlinx.html.br
import kotlinx.html.button
import kotlinx.html.h1
import kotlinx.html.h3
import kotlinx.html.head
import kotlinx.html.html
import kotlinx.html.id
import kotlinx.html.input
import kotlinx.html.label
import kotlinx.html.option
import kotlinx.html.pre
import kotlinx.html.script
import kotlinx.html.select
import kotlinx.html.stream.createHTML
import kotlinx.html.title
import world.respect.lib.xapi.model.XapiVerb
import world.respect.libutil.ext.resolve


fun HTML.demoAppLessonHtml(
    baseUrl: Url,
    gradeNum: Int,
    lessonNum: Int,
) {
    head {
        title { + "Grade $gradeNum Lesson $lessonNum" }

        script {
            type = "module"
            src = baseUrl.resolve("static/lesson_xapi.js").toString()
        }
    }

    body {
        h1 {
            + "Demo Learning Unit Grade $gradeNum Unit $lessonNum"
        }

        + "Actor"
        pre {
           id = "actor_info"
        }
        br()

        + "Activity id:"
        pre {
            id = "activity_id"
        }

        br()

        h3 {
            + "Send result (pass/fail)"
        }

        label {
            htmlFor = "score_text"
            + "Score"
        }

        input(type = InputType.text) {
            id = "score_text"
        }

        label {
            htmlFor = "verb_id"
        }

        select {
            id = "verb_id"
            listOf(XapiVerb.ID_PASSED, XapiVerb.ID_FAILED).forEach { verbId ->
                option {
                    value = verbId
                    + verbId.substringAfterLast("/")
                }
            }
        }

        button {
            id = "send_result_button"
            + "Send result statement"
        }
    }
}

class MakeDemoAppLessonHtmlUseCase {

    operator fun invoke(
        baseUrl: Url,
        gradeNum: Int,
        lessonNum: Int,
    ): String {
        return createHTML().html {
            demoAppLessonHtml(
                baseUrl = baseUrl,
                gradeNum = gradeNum,
                lessonNum = lessonNum
            )
        }
    }

    companion object {

        const val LESSON_HTML_FILENAME = "lesson.html"

    }
}