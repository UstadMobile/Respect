package world.respect.datalayer.db.school.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable


/**
 * Represents a category of content. Each category is tied to a category schema (e.g. category
 * * "level1" in the schema of "African Storybooks Reading Level"). This allows us to present the user
 * * with a dropdown list for each different schema.
 */
@Entity
@Serializable
class ContentCategory() {

    @PrimaryKey(autoGenerate = true)
    var contentCategoryUid: Long = 0

    var ctnCatContentCategorySchemaUid: Long = 0

    var name: String? = null

    var contentCategoryLocalChangeSeqNum: Long = 0

    var contentCategoryMasterChangeSeqNum: Long = 0

    var contentCategoryLastChangedBy: Int = 0

    var contentCategoryLct: Long = 0

    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        val category = other as ContentCategory?

        if (contentCategoryUid != category!!.contentCategoryUid) return false
        if (ctnCatContentCategorySchemaUid != category.ctnCatContentCategorySchemaUid) return false
        return if (name != null) name == category.name else category.name == null
    }

    override fun hashCode(): Int {
        var result = (contentCategoryUid xor contentCategoryUid.ushr(32)).toInt()
        result = 31 * result + (ctnCatContentCategorySchemaUid xor ctnCatContentCategorySchemaUid.ushr(32)).toInt()
        result = 31 * result + if (name != null) name!!.hashCode() else 0
        return result
    }

    companion object {

        const val TABLE_ID = 1
    }
}
