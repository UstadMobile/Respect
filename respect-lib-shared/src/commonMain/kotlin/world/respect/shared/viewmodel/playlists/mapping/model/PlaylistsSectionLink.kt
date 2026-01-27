package world.respect.shared.viewmodel.playlists.mapping.model

import io.ktor.http.Url
import kotlinx.serialization.Serializable

/**
 * @property href Absolute URL to the OPDS publication linked (NOT the Learning Unit ID URL).
 */
@Serializable
data class PlaylistsSectionLink(
    val uid: Long = System.currentTimeMillis(),
    val href: String,
    val title: String? = "",
    val appManifestUrl: Url? = null,
)
