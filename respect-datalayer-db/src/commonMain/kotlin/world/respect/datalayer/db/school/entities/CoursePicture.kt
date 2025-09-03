package world.respect.datalayer.db.school.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class CoursePicture(
    @PrimaryKey(autoGenerate = true)
    var coursePictureUid: Long = 0,

    var coursePictureLct: Long = 0,

    var coursePictureUri: String? = null,

    var coursePictureThumbnailUri: String? =null,

    var coursePictureActive: Boolean = true
) {

    companion object {

        const val TABLE_ID = 125
    }

}
