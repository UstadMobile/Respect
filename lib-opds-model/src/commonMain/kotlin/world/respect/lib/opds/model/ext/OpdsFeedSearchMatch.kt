package world.respect.lib.opds.model.ext

import kotlinx.serialization.Serializable

@Serializable
data class OpdsFeedSearchMatch(
    val groupIndex: Int,
    val index: Int,
    val isPublication: Boolean,
)