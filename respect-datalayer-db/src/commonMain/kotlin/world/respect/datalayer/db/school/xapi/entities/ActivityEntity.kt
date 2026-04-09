package world.respect.datalayer.db.school.xapi.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import world.respect.datalayer.school.xapi.model.XapiInteractionTypeEnum

@Entity
@Serializable
/**
 * @param actUid ActivityUid: the XXHash of actIdIri. The probability of a hash collision on a URL
 *        based ID using a 64bit hash is negligible.
 * @param actCorrectResponsePatterns the JSON of the correct responses pattern (array of strings)
 * @param actFlags In xAPI a property being omitted and a property being empty are not the same thing.
 *        In an SQL database that uses joins it impossible to tell the difference. This could be
 *        handled by adding a property here for each field e.g. choicesIsNull, scaleIsNull, etc.
 *        This can be done more efficiently using a single integer field with a bitmask and flag
 *        approach.
 */
data class ActivityEntity(

    @PrimaryKey
    val actUid: Long = 0,

    val actIdIri: String,

    val actType: String? = null,

    val actMoreInfo: String? = null,

    val actInteractionType: XapiInteractionTypeEnum? = null,

    val actCorrectResponsePatterns: String? = null,

    val actLct: Long = 0,

    val actFlags: Int = 0,

) {
    companion object {
        const val TYPE_UNSET = 0

        internal const val FLAG_CHOICES_NULL = 1

        internal const val FLAG_SCALE_NULL = 2

        internal const val FLAG_SOURCE_NULL = 4

        internal const val FLAG_TARGET_NULL = 8

        internal const val FLAG_STEPS_NULL = 16

        internal const val FLAG_EXTENSIONS_NULL = 32


        const val TABLE_ID = 64
    }
}

