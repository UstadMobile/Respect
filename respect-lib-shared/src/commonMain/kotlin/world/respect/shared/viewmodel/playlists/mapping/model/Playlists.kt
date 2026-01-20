package world.respect.shared.viewmodel.playlists.mapping.model

import io.ktor.http.Url
import kotlinx.serialization.Serializable
import world.respect.lib.opds.model.ReadiumSubjectObject

@Serializable
data class Playlists(
    val uid: Long = System.currentTimeMillis(),
    val title: String = "",
    val description: String = "",
    val subject: ReadiumSubjectObject? = null,
    val grade: String? = null,
    val language: String? = null,
    val sections: List<PlaylistsSection> = emptyList(),
    val createdBy: String? = null,
    val isSchoolWide: Boolean = false,
    val schoolUrl: Url? = null,
)
