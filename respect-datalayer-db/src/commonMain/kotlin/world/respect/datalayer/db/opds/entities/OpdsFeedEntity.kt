package world.respect.datalayer.db.opds.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.ktor.http.Url

/**
 * @property ofeUid the uid of the feed: will always be the xxhash of the URL
 */
@Entity
class OpdsFeedEntity(
    @PrimaryKey
    val ofeUid: Long,
    val ofeUrl: Url,
    val ofeUrlHash: Long,
    val ofeLastModifiedHeader: Long,
    val ofeEtag: String?,
) {

    companion object {

        const val TABLE_ID = 27
    }

}