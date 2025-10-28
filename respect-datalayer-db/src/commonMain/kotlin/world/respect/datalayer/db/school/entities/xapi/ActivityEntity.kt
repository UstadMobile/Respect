package world.respect.datalayer.db.school.entities.xapi

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity
@Serializable
/**
 * @param actCorrectResponsePatterns the JSON of the correct responses pattern (array of strings)
 */
data class ActivityEntity(

    @PrimaryKey
    var actUid: Long = 0,

    var actIdIri: String? = null,

    var actType: String? = null,

    var actMoreInfo: String? = null,

    var actInteractionType: Int = TYPE_UNSET,

    var actCorrectResponsePatterns: String? = null,

    var actLct: Long = 0,

    ) {
    companion object {
        const val TYPE_UNSET = 0
        const val TYPE_TRUE_FALSE = 1
        const val TYPE_CHOICE = 2
        const val TYPE_FILL_IN = 3
        const val TYPE_LONG_FILL_IN = 4
        const val TYPE_MATCHING = 5
        const val TYPE_PERFORMANCE = 6
        const val TYPE_SEQUENCING = 7
        const val TYPE_LIKERT = 8
        const val TYPE_NUMERIC = 9
        const val TYPE_OTHER = 10
        const val TABLE_ID = 64
    }
}

