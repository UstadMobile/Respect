package world.respect.datalayer.db.school.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(
    indices = arrayOf(
        Index("erClazzUid", "erStatus", name = "idx_enrolmentrequest_by_clazz"),
        Index("erPersonUid", "erStatus", name = "idx_enrolmentrequest_by_person"),
    )
)
@Serializable
data class EnrolmentRequest(
    @PrimaryKey(autoGenerate = true)
    var erUid: Long = 0,
    var erClazzUid: Long = 0,
    var erClazzName: String? = null,
    var erPersonUid: Long = 0,
    var erPersonFullname:  String? = null,
    var erPersonPictureUri: String? = null,
    var erPersonUsername: String? = null,
    var erRole: Int = 0,
    var erRequestTime: Long = 0,
    var erStatus: Int = STATUS_PENDING,
    var erStatusSetByPersonUid: Long = 0,
    var erDeleted: Boolean = false,
    var erStatusSetAuth: String? = null,
    var erLastModified: Long = 0,
) {

    companion object {

        const val STATUS_PENDING = 1

        const val STATUS_APPROVED = 2

        const val STATUS_REJECTED = 3

        const val STATUS_CANCELED = 4

        const val TABLE_ID = 10070

    }

}