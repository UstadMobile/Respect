package world.respect.datalayer.db.school.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import kotlin.time.Clock
import kotlin.time.Instant

@Entity(
    primaryKeys = ["ppPersonUid", "ppId"]
)
class PersonPasskeyEntity(

    val ppPersonUid: Long,

    val personPasskeyUid: Long,

    val ppId: String,

    val ppLastModified: Instant,

    val ppStored: Instant,

    val ppAttestationObj: String? = null,

    val ppClientDataJson: String? = null,

    val ppOriginString: String? = null,

    val ppChallengeString: String? = null,

    val ppPublicKey: String? = null,

    val isRevoked: Int = NOT_REVOKED,

    @ColumnInfo(defaultValue = "''")
    val ppDeviceName: String,

    @ColumnInfo(defaultValue = "0")
    val ppTimeCreated: Instant = Clock.System.now(),

    @ColumnInfo(defaultValue = "''")
    val ppAaguid: String = "",

    @ColumnInfo(defaultValue = "''")
    val ppProviderName: String = ""
){
    companion object {

        const val TABLE_ID = 30
        const val NOT_REVOKED = 0
        const val REVOKED = 1

    }
}