package world.respect.datalayer.db.school.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Represents an item that has been deleted. When an item is deleted:
 *
 * 1) The entity's deleted/inactive flag will be set
 * 2) A DeletedItem will be inserted
 *
 * The user can then go to the DeletedItemList to see deleted items to permanently delete them or
 * restore items.
 *
 */

@Entity(
    indices = [Index(
        "delItemStatus", "delItemTimeDeleted", name = "delitem_idx_status_time"
    )]
)
@Serializable
data class DeletedItem(
    @PrimaryKey(autoGenerate = true)
    var delItemUid: Long = 0,

    var delItemName: String? = null,

    var delItemIconUri: String? = null,

    var delItemLastModTime: Long = 0,

    var delItemTimeDeleted: Long = 0,

    var delItemEntityTable: Int = 0,

    var delItemEntityUid: Long = 0,

    var delItemDeletedByPersonUid: Long = 0,

    var delItemStatus: Int = STATUS_PENDING,

    @ColumnInfo(defaultValue = "0")
    var delItemIsFolder: Boolean = false,
) {
    companion object {

        const val TABLE_ID = 999

        const val STATUS_PENDING = 1

        const val STATUS_RESTORED = 2

        const val STATUS_DELETED_PERMANENTLY = 3
    }
}