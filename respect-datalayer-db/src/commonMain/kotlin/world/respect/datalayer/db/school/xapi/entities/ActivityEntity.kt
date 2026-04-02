package world.respect.datalayer.db.school.xapi.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import world.respect.datalayer.school.xapi.model.XapiInteractionTypeEnum

@Entity
@Serializable
/**
 * @param actCorrectResponsePatterns the JSON of the correct responses pattern (array of strings)
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

) {
    companion object {
        const val TYPE_UNSET = 0

        const val TABLE_ID = 64
    }
}

