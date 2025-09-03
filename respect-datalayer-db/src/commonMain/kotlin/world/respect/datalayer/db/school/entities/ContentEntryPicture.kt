package world.respect.datalayer.db.school.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity
@Serializable
open class ContentEntryPicture() {

    @PrimaryKey(autoGenerate = true)
    var cepUid: Long = 0

    var cepContentEntryUid: Long = 0

    var cepUri: String? = null

    var cepMd5: String? = null

    var cepFileSize: Int = 0

    var cepTimestamp: Long = 0

    var cepMimeType: String? = null

    var cepActive: Boolean = true

    companion object {

        const val TABLE_ID = 138
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as ContentEntryPicture

        if (cepUid != other.cepUid) return false
        if (cepContentEntryUid != other.cepContentEntryUid) return false
        if (cepUri != other.cepUri) return false
        if (cepMd5 != other.cepMd5) return false
        if (cepFileSize != other.cepFileSize) return false
        if (cepTimestamp != other.cepTimestamp) return false
        if (cepMimeType != other.cepMimeType) return false
        if (cepActive != other.cepActive) return false

        return true
    }

    override fun hashCode(): Int {
        var result = cepUid.hashCode()
        result = 31 * result + cepContentEntryUid.hashCode()
        result = 31 * result + (cepUri?.hashCode() ?: 0)
        result = 31 * result + (cepMd5?.hashCode() ?: 0)
        result = 31 * result + cepFileSize
        result = 31 * result + cepTimestamp.hashCode()
        result = 31 * result + (cepMimeType?.hashCode() ?: 0)
        result = 31 * result + cepActive.hashCode()
        return result
    }


}
