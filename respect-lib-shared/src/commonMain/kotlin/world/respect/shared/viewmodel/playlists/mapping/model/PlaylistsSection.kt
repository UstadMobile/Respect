package world.respect.shared.viewmodel.playlists.mapping.model

import kotlinx.serialization.Serializable

@Serializable
data class PlaylistsSection(
    val uid: Long = System.currentTimeMillis(),
    val title: String,
    val items: List<PlaylistsSectionLink> = emptyList()
)
