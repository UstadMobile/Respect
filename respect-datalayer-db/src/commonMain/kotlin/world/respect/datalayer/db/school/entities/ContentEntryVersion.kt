package world.respect.datalayer.db.school.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Represents an available version of a ContentEntry. This actual (binary) content assets may be
 * stored on the same endpoint as the database, or it could be stored on any other http server.
 *
 */
@Entity
@Serializable
data class ContentEntryVersion(
    @PrimaryKey(autoGenerate = true)
    var cevUid: Long = 0,

    /**
     * The related ContentEntryUid
     */
    var cevContentEntryUid: Long = 0,

    /**
     * The Uri of the item that should be opened e.g.
     *
     *  The tincan.xml file for xapi content
     *  The PDF file for PDF content
     *  Media JSON info for video files
     *  OPF for epub content
     *
     *  The Uri should match ContentManifestEntry.uri for the entry to open
     */
    var cevOpenUri: String? = "",

    /**
     * The content type that will be used to determine what screen will be used to display the
     * content. This can use TYPE_ presets (or content type could be added to import plugins etc).
     */
    var cevContentType: String? = "",

    /**
     * The URL for the ContentManifest (as per com.ustadmobile.core.contentformats.manifest.ContentManifest).
     *
     * It may be stored on the same server as the one holding this database entity, or a different
     * server. See ARCHITECTURE.md for more info on the offline content architecture.
     *
     * e.g. https://endpoint.com/api/content/cevUid/_ustadmanifest.json
     */
    var cevManifestUrl: String? = null,

    /**
     * The estimated total size (in bytes)
     */
    var cevSize: Long = 0,

    var cevInActive: Boolean = false,

    /**
     * The last modified of the actual content
     */
    var cevLastModified: Long = 0,

    var cevLct: Long = 0,

    /**
     * The indicative storage size (e.g. size of all entries) - e.g. the sum of all included
     * ContentManifestEntry.storageSize
     */
    @ColumnInfo(defaultValue = "0")
    var cevStorageSize: Long = 0,

    /**
     * The size of the original file e.g. when it was imported before any extra compression.
     */
    @ColumnInfo(defaultValue = "0")
    var cevOriginalSize: Long = 0,
) {

    companion object {

        @Suppress("unused")
        const val TYPE_EPUB = "epub"

        @Suppress("unused")
        const val TYPE_VIDEO = "video"

        const val TYPE_PDF = "pdf"

        @Suppress("unused")
        const val TYPE_XAPI = "xapi"

        const val PATH_POSTFIX = "api/content/"

        const val TABLE_ID = 738

    }
}