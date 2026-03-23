package world.respect.datalayer.db.school.xapi.entities

import androidx.room.Entity
import kotlinx.serialization.Serializable

/**
 * Represents an interaction component as per
 * https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Data.md#interaction-activities
 *
 * Used as a 1:many join with ActivityEntity (aieActivityUid is the foreign key)
 *
 * @param aieActivityUid activity uid (foreign key)
 * @param aieHash hash of "$aieProp$aieId" used to uniquely identify the Interaction component
 *        within a given Object.
 * @param aieProp one of the PROP_ constants
 * @param aieId the id of this choice as per the spec
 * @param aieLastMod last modified time.
 */
@Entity(
    primaryKeys = ["aieActivityUid", "aieHash"]
)
@Serializable
data class ActivityInteractionEntity(
    val aieActivityUid: Long = 0,

    val aieHash: Long = 0,

    val aieProp: Int = 0,

    val aieId: String? = null,

    val aieLastMod: Long = 0,

    val aieIsDeleted: Boolean = false,
) {

    companion object {
        const val PROP_CHOICES = 1
        const val PROP_SCALE = 2
        const val PROP_SOURCE = 3
        const val PROP_TARGET = 4
        const val PROP_STEPS = 5
        const val TABLE_ID = 6401
    }
}

