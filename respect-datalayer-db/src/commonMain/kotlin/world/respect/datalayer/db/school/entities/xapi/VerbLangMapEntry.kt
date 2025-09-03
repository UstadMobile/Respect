package world.respect.datalayer.db.school.entities.xapi

import androidx.room.Entity
import kotlinx.serialization.Serializable

@Entity(
    primaryKeys = ["vlmeVerbUid", "vlmeLangHash"]
)
@Serializable
/**
 * Use in a one to many join with VerbEntity. Verb display can be updated. When statements are
 * queried using canonical mode, we need to be able to return the the latest display langmap.
 *
 * @param vlmeVerbUid the foreign key e.g. VerbEntity.verbUid (xxhash of the Verb's id url)
 * @param vlmeLangHash the xxhash of the language code as per the lang map e.g. en-US
 * @param vlmeEntryString the actual string e.g. as will be displayed to the user e.g. 'Completed'
 * @param vlmeLangCode the lang code as per the Language Map
 * @param vlmeLastModified the last time this entry was modified
 */
data class VerbLangMapEntry(
    var vlmeVerbUid: Long = 0L,

    var vlmeLangHash: Long = 0L,

    var vlmeLangCode: String? = null,

    var vlmeEntryString: String? = null,

    var vlmeLastModified: Long = 0,
) {
    companion object {
        const val TABLE_ID = 620
    }
}
