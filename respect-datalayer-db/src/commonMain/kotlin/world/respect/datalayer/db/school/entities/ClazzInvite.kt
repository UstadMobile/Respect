package world.respect.datalayer.db.school.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable


@Entity
@Serializable
data class ClazzInvite(
    @PrimaryKey(autoGenerate = true)
    var ciUid: Long = 0,

    var ciPersonUid: Long = 0,

    var ciRoleId: Long = 0,

    var ciClazzUid: Long = 0,

    @ColumnInfo(defaultValue = "1")
    var inviteType: Int = 1,

    var inviteContact: String = "",

    var inviteToken: String = "",

    var inviteStatus: Int = STATUS_PENDING,

    var inviteExpire: Long = 0,

    var inviteLct: Long = 0
) {
    companion object {
        const val TABLE_ID = 521

        const val EMAIL = 1
        const val PHONE = 2
        const val INTERNAL_MESSAGE = 3
        const val STATUS_PENDING = 0
        const val STATUS_ACCEPTED = 1
        const val STATUS_DECLINED = 2
        const val STATUS_REVOKED = 3
    }
}