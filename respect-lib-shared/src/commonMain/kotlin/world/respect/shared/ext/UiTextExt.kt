package world.respect.shared.ext

import org.jetbrains.compose.resources.getString
import world.respect.shared.resources.StringResourceUiText
import world.respect.shared.resources.StringUiText
import world.respect.shared.resources.UiText

suspend fun UiText.asString(): String {
    return when(this) {
        is StringResourceUiText -> getString(resource)
        is StringUiText -> text
    }
}