package world.respect.datalayer.school.model.composites

import kotlinx.serialization.Serializable

@Serializable
data class OpdsFeedSearchMatch(
    val groupIndex: Int,
    val index: Int,
    val isPublication: Boolean,
)
