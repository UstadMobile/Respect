package world.respect.datalayer.school.domain

import com.eygraber.uri.Uri
import io.ktor.http.Url
import world.respect.datalayer.school.opds.ext.withAbsoluteSelfUrl
import world.respect.lib.opds.model.OpdsFeed
import world.respect.lib.opds.model.ReadiumLink
import world.respect.libutil.ext.appendEndpointSegments
import world.respect.libutil.util.time.systemTimeInMillis
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Modify an OPDS feed to make it suitable for use as a school-based playlist. This will:
 * 1) Change the URL to the schoolurl/playlist/uuid
 *    e.g. https://schoolname.example.org/playlist/00112233-4455-6677-8899-aabbccddeeff
 * 2) Set the last modified time.
 * 3) Add an owner link to identify the creator of the playlist.
 */
class MakePlaylistOpdsFeedUseCase(
    private val schoolUrl: Url
) {

    @OptIn(ExperimentalUuidApi::class)
    operator fun invoke(
        base: OpdsFeed,
        username: String,
        uuid: Uuid = Uuid.random(),
    ): OpdsFeed {
        val feedUrl = schoolUrl.appendEndpointSegments("playlist/$uuid")

        val ownerLink = ReadiumLink(
            href = "${schoolUrl}user/$username",
            rel = listOf(REL_OWNER),
        )

        return base.copy(
            metadata = base.metadata.copy(
                identifier = Uri.parseOrNull(feedUrl.toString()),
                modified = Instant.fromEpochMilliseconds(systemTimeInMillis()),
            ),
            links = base.links + ownerLink,
        ).withAbsoluteSelfUrl(feedUrl)
    }

    companion object {
        const val REL_OWNER = "https://respect.ustadmobile.com/ns/owner"

    }
}