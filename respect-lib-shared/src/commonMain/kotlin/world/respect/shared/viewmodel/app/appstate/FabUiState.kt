package world.respect.shared.viewmodel.app.appstate

/**
 * Represents the Floating Action Button.
 */
data class FabUiState(
    val visible: Boolean = false,
    val text: String? = null,
    val icon: FabIcon = FabIcon.NONE,
    val onClick: () -> Unit = { },
) {

    enum class FabIcon {
        NONE, ADD, EDIT,APP
    }

}