package world.respect.datalayer.db.school.entities.xapi

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity
/**
 * Verb as per the xAPI spec. Verb only has two properties ( id and display ) as per the spec:
 * https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Data.md#243-verb
 *
 * Joins with VerbXLangMapEntry to handle the display langmap
 *
 * @param verbUid The XXHash64 of verbUrlId
 */
data class VerbEntity(
    @PrimaryKey
    var verbUid: Long = 0,

    var verbUrlId: String? = null,

    var verbDeleted: Boolean = false,

    var verbLct: Long = 0,
) {
    companion object {
        const val TABLE_ID = 62
        const val VERB_COMPLETED_URL = "http://adlnet.gov/expapi/verbs/completed"
        const val VERB_PASSED_URL = "http://adlnet.gov/expapi/verbs/passed"
        const val VERB_FAILED_URL = "http://adlnet.gov/expapi/verbs/failed"
        const val VERB_EXPERIENCED_URL = "http://adlnet.gov/expapi/verbs/experienced"
    }
}
