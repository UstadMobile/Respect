package world.respect.datalayer.db.school.xapi.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import world.respect.datalayer.school.model.StatusEnum

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
    val verbUid: Long,
    val verbUrlId: String,
    val verbStatus: StatusEnum = StatusEnum.ACTIVE,
    val verbLct: Long = 0,
) {
    companion object {
        const val TABLE_ID = 62
        const val VERB_COMPLETED_URL = "http://adlnet.gov/expapi/verbs/completed"
        const val VERB_PASSED_URL = "http://adlnet.gov/expapi/verbs/passed"
        const val VERB_FAILED_URL = "http://adlnet.gov/expapi/verbs/failed"
        const val VERB_EXPERIENCED_URL = "http://adlnet.gov/expapi/verbs/experienced"
    }
}
