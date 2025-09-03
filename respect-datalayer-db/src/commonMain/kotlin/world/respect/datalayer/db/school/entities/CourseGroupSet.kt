package world.respect.datalayer.db.school.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity
@Serializable
class CourseGroupSet {

    @PrimaryKey(autoGenerate = true)
    var cgsUid: Long = 0

    var cgsName: String? = null

    var cgsTotalGroups: Int = 4

    var cgsActive: Boolean = true

    @ColumnInfo(index = true)
    var cgsClazzUid: Long = 0

    var cgsLct: Long = 0

    companion object {

        const val TABLE_ID = 242


    }


}