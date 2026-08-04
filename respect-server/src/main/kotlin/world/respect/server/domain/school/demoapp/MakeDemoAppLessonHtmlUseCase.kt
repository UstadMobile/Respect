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
import kotlinx.html.meta
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

        meta {
            name = "viewport"
            content = "width=device-width, initial-scale=1"
        }
    }

    body {
        h1 {
            + "Demo Learning Unit Grade $gradeNum Unit $lessonNum"
        }

        pre {
           id = "actor_info"
        }

        pre {
            id = "activity_id"
        }

        h3 {
            + "Send result (pass/fail)"
        }

        label {
            htmlFor = "score_text"
            + "Scaled score (must be between 0 and 1):"
        }

        br()

        input(type = InputType.text) {
            id = "score_text"
        }

        br()

        label {
            htmlFor = "verb_id"
            +"Result:"
        }
        br()

        select {
            id = "verb_id"
            listOf(XapiVerb.ID_PASSED, XapiVerb.ID_FAILED).forEach { verbId ->
                option {
                    value = verbId
                    + verbId.substringAfterLast("/")
                }
            }
        }
        br()
        br()
        button {
            id = "send_result_button"
            + "Send result statement"
        }

        pre {
            id = "send_result_result"
        }

        h3 {
            + "Send completed statement"
        }

        button {
            id = "send_completed_button"

            + "Send completed statement"
        }

        pre {
            id = "send_completed_result"
        }


        h3 {
            + "Send progressed statement"
        }

        label {
            htmlFor = "progress_text"
            + "Progress (must be between 0 and 100):"
        }

        br()

        input(type = InputType.text) {
            id = "progress_text"
        }

        br()

        button {
            id = "send_progress_button"
            + "Send progressed statement"
        }

        pre {
            id = "send_progress_result"
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