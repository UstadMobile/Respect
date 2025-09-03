package world.respect.datalayer.db.school.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity

@Serializable
data class CourseGroupMember(
    @PrimaryKey(autoGenerate = true)
    var cgmUid: Long = 0,

    var cgmSetUid: Long = 0,

    // real group numbers start from 1, 0 means this person is not yet in a group
    var cgmGroupNumber: Int = 0,

    var cgmPersonUid: Long = 0,

    var cgmLct: Long = 0,
) {

    companion object {

        const val TABLE_ID = 243


    }

}