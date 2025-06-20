package world.respect

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import world.respect.app.app.App

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Respect",
    ) {
        App()
    }
}