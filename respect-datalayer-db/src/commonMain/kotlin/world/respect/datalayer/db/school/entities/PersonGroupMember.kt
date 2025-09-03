package world.respect.datalayer.db.school.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity
@Serializable
class PersonGroupMember() {


    @PrimaryKey(autoGenerate = true)
    var groupMemberUid: Long = 0


    var groupMemberActive: Boolean = true

    @ColumnInfo(index = true)
    var groupMemberPersonUid: Long = 0

    @ColumnInfo(index = true)
    var groupMemberGroupUid: Long = 0

    var groupMemberMasterCsn: Long = 0

    var groupMemberLocalCsn: Long = 0

    var groupMemberLastChangedBy: Int = 0

    var groupMemberLct: Long = 0

    constructor(personUid:Long, groupUid:Long) : this(){
        this.groupMemberPersonUid = personUid
        this.groupMemberGroupUid = groupUid
    }

    companion object {
        const val TABLE_ID = 44
    }
}
