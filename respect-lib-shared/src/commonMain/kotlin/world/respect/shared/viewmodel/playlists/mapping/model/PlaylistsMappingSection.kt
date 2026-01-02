package world.respect.shared.viewmodel.playlists.mapping.model

import kotlinx.serialization.Serializable

@Serializable
data class PlaylistsMappingSection(
    val uid: Long = System.currentTimeMillis(),
    val title: String,
    val items: List<PlaylistsMappingSectionLink> = emptyList()
)
