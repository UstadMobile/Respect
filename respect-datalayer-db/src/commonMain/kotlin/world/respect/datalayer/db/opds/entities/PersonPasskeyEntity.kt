package world.respect.datalayer.db.opds.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
 class PersonPasskeyEntity(
    @PrimaryKey(autoGenerate = true)
    var personPasskeyUid: Long = 0,

    var ppPersonUid: Long = 0,

    var ppAttestationObj: String? = null,

    var ppClientDataJson: String? = null,

    var ppOriginString: String? = null,

    var ppId: String? = null,

    var ppChallengeString: String? = null,

    var ppPublicKey: String? = null,

    var isRevoked: Int = NOT_REVOKED,


){
    companion object {

        const val TABLE_ID = 30
        const val NOT_REVOKED = 0
        const val REVOKED = 1

    }
}