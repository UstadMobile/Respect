package world.respect.datalayer.db.opds.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity
@Serializable
class PersonPasskeyEntity(
    @PrimaryKey(autoGenerate = true)
    val personPasskeyUid: Long = 0,

    val ppPersonUid: Long = 0,

    val ppAttestationObj: String? = null,

    val ppClientDataJson: String? = null,

    val ppOriginString: String? = null,

    val ppId: String? = null,

    val ppChallengeString: String? = null,

    val ppPublicKey: String? = null,

    val isRevoked: Int = NOT_REVOKED,


){
    companion object {

        const val TABLE_ID = 30
        const val NOT_REVOKED = 0
        const val REVOKED = 1

    }
}