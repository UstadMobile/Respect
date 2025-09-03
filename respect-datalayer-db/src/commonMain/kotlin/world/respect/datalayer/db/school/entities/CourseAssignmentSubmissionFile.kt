package world.respect.datalayer.db.school.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class CourseAssignmentSubmissionFile(
    @PrimaryKey(autoGenerate = true)
    var casaUid: Long = 0,

    var casaSubmissionUid: Long = 0,

    @ColumnInfo(defaultValue = "0")
    var casaSubmitterUid: Long = 0,

    //Assignment Uid
    var casaCaUid: Long = 0,

    var casaClazzUid: Long = 0,

    var casaMimeType: String? = null,

    var casaFileName: String? = null,

    var casaUri: String? = null,

    var casaSize: Int = 0,

    var casaTimestamp: Long = 0,

    @ColumnInfo(defaultValue = "0")
    var casaDeleted: Boolean = false,

) {

    companion object {

        const val TABLE_ID = 90
    }

}