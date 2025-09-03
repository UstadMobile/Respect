package world.respect.datalayer.db.school.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Represents the site as a whole. There is only ever one row. Note trigger SQL checks to make sure
 * that there is never any change to the authSalt. That MUST remain constant otherwise authentication
 * will be broken.
 */
@Entity
@Serializable
class Site {

    @PrimaryKey(autoGenerate = true)
    var siteUid: Long = 0

    var sitePcsn: Long = 0

    var siteLcsn: Long = 0

    var siteLcb: Int = 0

    var siteLct: Long = 0

    var siteName: String? = null

    var guestLogin: Boolean = true

    var registrationAllowed: Boolean = true

    var authSalt: String? = null

    companion object {

        const val TABLE_ID = 189

    }

}