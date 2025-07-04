package world.respect.datasource.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents entries in a LangMap structure as per
 * https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Data.md#42-language-maps
 *
 * LangMaps are used on the RespectAppManifest, OPDS, and various Xapi entities.
 *
 * @property lmeTableId a unique table id (e.g. using TABLE_ID constants)
 * @property lmeEntityUid1 the uid of the related entity (e.g. RespectAppManifest, OpdsPublication, etc)
 * @property lmeEntityUid2 second uid of the related entity, where applicable (e.g. Xapi Statements UUID)
 * @property lmePropId Some entities have multiple properties that use a LangMap, this field can be used to differentiate
 * @property lmeLang Language code (e.g. en)
 * @property lmeRegion Region code (e.g. US)
 * @property lmeValue the actual string value
 */
@Entity(
    indices = [Index(value = ["lmeTableId", "lmeEntityUid1", "lmeEntityUid2", "lmePropId"])]
)
data class LangMapEntity(
    @PrimaryKey(autoGenerate = true)
    val lmeId: Long = 0,
    val lmeTableId: Int,
    val lmeEntityUid1: Long,
    val lmeEntityUid2: Long = 0,
    val lmePropId: Long = 0,
    val lmeLang: String,
    val lmeRegion: String?,
    val lmeValue: String,
) {

    companion object {

        const val LANG_NONE = ""

    }

}