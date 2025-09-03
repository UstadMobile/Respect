package world.respect.datalayer.db.school.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity
open class CourseTerminology {

    @PrimaryKey(autoGenerate = true)
    var ctUid: Long = 0

    var ctTitle: String? = null

    /**
     * A json map of keys as per TerminologyKeys to the terminology to use for this course.
     *
     * see CourseTerminologyStrings (in core)
     */
    var ctTerminology: String? = null

    var ctLct: Long = 0

    companion object {

        const val TABLE_ID = 450


    }

}