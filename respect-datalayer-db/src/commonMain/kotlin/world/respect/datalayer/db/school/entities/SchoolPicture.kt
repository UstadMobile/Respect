package world.respect.datalayer.db.school.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity
@Serializable
open class SchoolPicture() {

    @PrimaryKey(autoGenerate = true)
    var schoolPictureUid: Long = 0

    //This is not really used. This is effectively a 1:1 join. schoolPictureUid should equal
    // the uid of the school itself.
    var schoolPictureSchoolUid : Long = 0

    var schoolPictureMasterChangeSeqNum: Long = 0

    var schoolPictureLocalChangeSeqNum: Long = 0


    var schoolPictureLastChangedBy: Int = 0

    var schoolPictureLct: Long = 0

    var schoolPictureFileSize : Long = 0

    var schoolPictureTimestamp : Long = 0

    var schoolPictureMimeType : String = ""

    companion object {

        const val TABLE_ID = 175
    }
}
