package world.respect.datalayer.db.school.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class ContentEntryPicture2(
    /**
     * Should equal the contentEntryUid of the contententry this picture is for
     */
    @PrimaryKey
    var cepUid: Long = 0,

    var cepLct: Long = 0,

    var cepPictureUri: String? = null,

    var cepThumbnailUri: String? = null,
) {
    companion object {

        const val TABLE_ID = 6678


    }
}
