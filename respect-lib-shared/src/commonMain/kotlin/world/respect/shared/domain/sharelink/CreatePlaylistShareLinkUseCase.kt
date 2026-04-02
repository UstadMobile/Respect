package world.respect.shared.domain.sharelink

import io.ktor.http.URLBuilder
import io.ktor.http.Url
import world.respect.libutil.ext.RESPECT_SCHOOL_LINK_SEGMENT
import world.respect.libutil.ext.appendEndpointPathSegments

class CreatePlaylistShareLinkUseCase(
    private val schoolUrl: Url,
) {
    operator fun invoke(playlistUrl: String): Url {
        val playlistUuid = playlistUrl
            .trimEnd('/')
            .substringAfterLast('/')
            .takeIf { it.isNotBlank() }
            ?: throw IllegalArgumentException(
                "Cannot extract playlist UUID from URL: $playlistUrl"
            )

        return URLBuilder(schoolUrl).apply {
            appendEndpointPathSegments(
                listOf(RESPECT_SCHOOL_LINK_SEGMENT, PATH, playlistUuid)
            )
        }.build()
    }

    companion object {
        const val PATH = "playlist"
    }
}