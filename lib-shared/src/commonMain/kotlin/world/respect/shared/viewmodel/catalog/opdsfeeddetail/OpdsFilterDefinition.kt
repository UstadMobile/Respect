package world.respect.shared.viewmodel.catalog.opdsfeeddetail

import world.respect.shared.resources.UiText

data class OpdsFilterDefinition(
    val key: String,
    val label: UiText,
    val options: Map<String, String>,
)
