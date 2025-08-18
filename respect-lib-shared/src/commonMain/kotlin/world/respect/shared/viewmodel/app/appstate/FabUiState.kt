package world.respect.shared.viewmodel.app.appstate

import world.respect.shared.resources.UiText

/**
 * Represents the Floating Action Button.
 */
data class FabUiState(
    val visible: Boolean = false,
    val text: UiText? = null,
    val icon: FabIcon = FabIcon.NONE,
    val onClick: () -> Unit = { },
) {

    enum class FabIcon {
        NONE, ADD, EDIT,APP
    }

}