package world.respect.shared.domain.feedback


import org.jetbrains.compose.resources.StringResource
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.category_integrated_apps
import world.respect.shared.generated.resources.category_launcher
import world.respect.shared.generated.resources.category_other
import world.respect.shared.generated.resources.category_question
import world.respect.shared.generated.resources.category_rate_us
import world.respect.shared.resources.StringResourceUiText
import world.respect.shared.resources.UiText

enum class FeedbackCategory {
    LAUNCHER,
    INTEGRATED_APPS,
    QUESTION,
    RATE_US,
    OTHER;

    companion object {

        fun getStringResource(category: FeedbackCategory): StringResource {
            entries.forEach { _ ->
                return when (category) {
                    LAUNCHER -> Res.string.category_launcher
                    INTEGRATED_APPS -> Res.string.category_integrated_apps
                    QUESTION -> Res.string.category_question
                    RATE_US ->  Res.string.category_rate_us
                    OTHER -> Res.string.category_other
                }
            }
            return Res.string.category_other
        }

        fun getStringResourceUiText(category: FeedbackCategory): UiText {
            return StringResourceUiText(getStringResource(category))
        }
    }
}


