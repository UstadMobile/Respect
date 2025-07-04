package world.respect.app.appstate

data class Snack(
    val message: String,
    val action: String? = null,
    val onAction: (() -> Unit)? = null,
)
