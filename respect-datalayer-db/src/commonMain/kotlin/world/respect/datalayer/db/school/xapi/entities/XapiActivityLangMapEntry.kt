package world.respect.datalayer.db.school.xapi.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.Instant

@Serializable
@Entity(
    indices = [
        Index(
            value = ["almeActivityUid", "almeProperty", "almeInteractionId", "almeLangCode"],
            unique = true
        )
    ]
)
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
 * @property almeLastModified the time when this entry was last modified: used for conflict resolution
 */
data class XapiActivityLangMapEntry(
    @PrimaryKey(autoGenerate = true)
    val almeUid: Long = 0,

    val almeActivityUid: Long = 0,

    val almeProperty: XapiActivityLangMapEntryPropEnum,

    val almeInteractionId: String?,

    val almeLangCode: String,

    val almeValue: String,

    val almeLastModified: Instant = Clock.System.now(),

    ) {
    companion object {
        const val TABLE_ID = 6442
    }
}

