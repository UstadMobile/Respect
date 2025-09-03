package world.respect.datalayer.db.school.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Represents an item that a user has selected for offline access on a particular device (e.g. node).
 *
 * This can be used in queries/triggers to download blobs (e.g. profile pictures, course pictures, etc)
 * as required. It can also be used with ContentEntryVersion to trigger the download of a
 * ContentEntryVersion when the OfflineItem is created and when a new ContentEntryVersion is
 * available.
 *
 * This can also be used on the server side to select data to push to given clients.
 */
@Entity(
    indices = [Index("oiNodeId", "oiContentEntryUid", name = "offline_item_node_content_entry")]
)
@Serializable
data class OfflineItem(
    @PrimaryKey(autoGenerate = true)
    var oiUid: Long = 0,
    var oiNodeId: Long = 0,
    var oiClazzUid: Long = 0,
    var oiCourseBlockUid: Long = 0,
    var oiContentEntryUid: Long = 0,
    var oiActive: Boolean = true,
    var oiLct: Long = 0,
) {
    companion object {

        const val TABLE_ID = 971

    }
}
