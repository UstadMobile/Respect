package world.respect.datalayer.db.school.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable


/**
 * Join entity to link ContentEntry many:many with ContentCategory
 */
@Entity
@Serializable
class ContentEntryContentCategoryJoin() {

    @PrimaryKey(autoGenerate = true)
    var ceccjUid: Long = 0

    @ColumnInfo(index = true)
    var ceccjContentEntryUid: Long = 0

    var ceccjContentCategoryUid: Long = 0

    var ceccjLocalChangeSeqNum: Long = 0

    var ceccjMasterChangeSeqNum: Long = 0

    var ceccjLastChangedBy: Int = 0

    var ceccjLct: Long = 0

    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        val that = other as ContentEntryContentCategoryJoin?

        if (ceccjUid != that!!.ceccjUid) return false
        return if (ceccjContentEntryUid != that.ceccjContentEntryUid) false else ceccjContentCategoryUid == that.ceccjContentCategoryUid
    }

    override fun hashCode(): Int {
        var result = (ceccjUid xor ceccjUid.ushr(32)).toInt()
        result = 31 * result + (ceccjContentEntryUid xor ceccjContentEntryUid.ushr(32)).toInt()
        result = 31 * result + (ceccjContentCategoryUid xor ceccjContentCategoryUid.ushr(32)).toInt()
        return result
    }

    companion object {

        const val TABLE_ID = 3
    }
}
