package world.respect.datalayer.db.opds.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.ktor.http.Url
import kotlin.time.Clock
import kotlin.time.Instant

/**
 *
 *
 * @property ofeUid the uid of the feed: will always be the xxhash of the URL
 * @property ofeUrl: the URL of the feed (as per the self link)
 * @property ofeLastModified: the time the feed was actually modified: as per OpdsFeedMetadata.modified
 *           This matches with ModelWithTimes.modified.
 * @property ofeStored: the time the feed was stored in the database: as per ModelWithTimes.stored
 */
@Entity
data class OpdsFeedEntity(
    @PrimaryKey
    val ofeUid: Long,
    val ofeUrl: Url,
    val ofeUrlHash: Long,
    val ofeLastModified: Instant,
    val ofeEtag: String?,
    val ofeStored: Instant = Clock.System.now(),
) {

    companion object {

        const val TABLE_ID = 27
    }

}