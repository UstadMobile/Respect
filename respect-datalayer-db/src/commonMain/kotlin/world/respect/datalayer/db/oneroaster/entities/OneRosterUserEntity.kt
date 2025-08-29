package world.respect.datalayer.db.oneroaster.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class OneRosterUserEntity(
    @PrimaryKey
    val userSourcedId: String,
    val userStatus: String,
    val userDateLastModified: Long,
    val userMetadata: String? = null,
    val userMasterIdentifier: String? = null,
    val username: String? = null,
    val userIds: String? = null,
    val enabledUser: Boolean = true,
    val givenName: String,
    val familyName: String,
    val middleName: String? = null,
    val preferredFirstName: String? = null,
    val preferredMiddleName: String? = null,
    val preferredLastName: String? = null,
    val pronouns: String? = null,
    val roles: String? = null,
    val userProfiles: String? = null,
    val identifier: String? = null,
    val email: String? = null,
    val sms: String? = null,
    val phone: String? = null,
    val grades: String? = null,
    val password: String? = null,
    val resources: String? = null,
)
