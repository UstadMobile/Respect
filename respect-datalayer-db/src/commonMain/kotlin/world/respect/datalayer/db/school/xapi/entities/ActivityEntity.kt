package world.respect.datalayer.db.school.xapi.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import world.respect.datalayer.school.xapi.model.XapiInteractionTypeEnum

@Entity
@Serializable
/**
 * @param actCorrectResponsePatterns the JSON of the correct responses pattern (array of strings)
 * @param actFlags In xAPI a property being omitted and a property being empty are not the same thing.
 *        In an SQL database that uses joins it impossible to tell the difference. Using a single
 *        Integer as bitmask flags avoids the need for multiple separate fields on the database.
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

        internal const val FLAG_NAME_NULL = 1

        internal const val FLAG_DESCRIPTION_NULL = 2

        internal const val FLAG_EXTENSIONS_NULL = 4


        const val TABLE_ID = 64
    }
}

