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

enum class FeedbackCategory(val resource: StringResource) {
    LAUNCHER(Res.string.category_launcher),
    INTEGRATED_APPS(Res.string.category_integrated_apps),
    QUESTION(Res.string.category_question),
    OTHER(Res.string.category_other);


    val uiText: UiText get() = StringResourceUiText(resource)
}
