package world.respect.datalayer.db.school.xapi.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import world.respect.datalayer.school.xapi.model.XapiInteractionType

@Serializable
@Entity
/**
 * As per Activity Definition:
 * https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Data.md#activity-definition
 *
 * An Activity has a Name and Description property that are LangMaps. The Activity can also have
 * interaction properties (e.g. choices), each of which can have a name.
 *
 * ActivityLangMapEntry is used to store all of these.
 *
 * @param almeActivityUid the activity uid that this lang map entry is related to
 * @param almeLangCode the lang code as per the xAPI language map eg en-US
 * @param almeProperty the property that this LangMapEntry is for : see ActivityLangMapEntryPropEnum
 * @param almeInteractionId if this is an entry for an interaction, then the id of the interaction
 * @param almeValue the string value for the given language
 */
data class ActivityLangMapEntry(
    @PrimaryKey(autoGenerate = true)
    val almeUid: Long = 0,

    val almeActivityUid: Long = 0,

    val almeProperty: ActivityLangMapEntryPropEnum,

    val almeInteractionId: String?,

    val almeLangCode: String,

    val almeValue: String,

) {
    companion object {
        const val TABLE_ID = 6442
    }
}

