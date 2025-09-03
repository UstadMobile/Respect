package world.respect.datalayer.db.school.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class CourseBlockPicture(
    /**
     * Id must be the same as CourseBlock cbUid
     */
    @PrimaryKey
    var cbpUid: Long = 0,

    var cbpLct: Long = 0,

    var cbpPictureUri: String? = null,

    var cbpThumbnailUri: String? = null,

)  {

    companion object {

        const val TABLE_ID = 6677


    }

}
