package world.respect.datalayer.db.school.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Represents a group in the system. Each individual user also has their own one member group.
 */
@Entity
@Serializable
open class PersonGroup() {

    @PrimaryKey(autoGenerate = true)
    var groupUid: Long = 0

    var groupMasterCsn: Long = 0

    var groupLocalCsn: Long = 0

    var groupLastChangedBy: Int = 0

    var groupLct: Long = 0

    var groupName: String? = null

    var groupActive : Boolean = true

    /**
     *
     */
    var personGroupFlag: Int = 0

    constructor(name: String) : this() {
        this.groupName = name
    }

    companion object{

        const val TABLE_ID = 43

        @Suppress("unused") //Reserved for future use
        const val PERSONGROUP_FLAG_DEFAULT = 0

        const val PERSONGROUP_FLAG_PERSONGROUP = 1

        @Suppress("unused") //Reserved for future use
        const val PERSONGROUP_FLAG_PARENT_GROUP = 2

        @Suppress("unused") //Reserved for future use
        const val PERSONGROUP_FLAG_STUDENTGROUP = 4

        @Suppress("unused") //Reserved for future use
        const val PERSONGROUP_FLAG_TEACHERGROUP = 8

        const val PERSONGROUP_FLAG_GUESTPERSON = 16


    }
}
