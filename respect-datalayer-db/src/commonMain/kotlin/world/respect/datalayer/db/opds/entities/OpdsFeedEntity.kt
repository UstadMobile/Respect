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
 * @property ofeStored: the time the feed was stored in the database: as per ModelWithTimes.stored
 */
@Entity
class OpdsFeedEntity(
    @PrimaryKey
    val ofeUid: Long,
    val ofeUrl: Url,
    val ofeUrlHash: Long,
    val ofeLastModifiedHeader: Long, //Change this to Instant. Will also effectively be the stoerd time
    val ofeEtag: String?,
    val ofeStored: Instant = Clock.System.now(),
) {

    companion object {

        const val TABLE_ID = 27
    }

}