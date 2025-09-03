package world.respect.datalayer.db.school.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class PersonPicture(
    /**
     * The personPictureUid should match the personUid
     */
    @PrimaryKey(autoGenerate = true)
    var personPictureUid: Long = 0,

    var personPictureLct: Long = 0,

    var personPictureUri: String? = null,

    var personPictureThumbnailUri: String? = null,

    var fileSize: Int = 0,

    var personPictureActive: Boolean = true,

) {

    companion object {

        const val TABLE_ID = 50
    }


}
