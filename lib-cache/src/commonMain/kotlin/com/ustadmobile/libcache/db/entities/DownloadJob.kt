package com.ustadmobile.libcache.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import io.ktor.http.Url
import kotlinx.serialization.Serializable

/**
 * To be used by BlobUploadClient and content downloader. Provides database storage so that:
 *  - The job can be enqueued and run at an appropriate time (e.g. when connectivity is available)
 *  - The job can be interrupted and resumed
 *  - The progress of the job can be displayed to the user.
 *
 * A TransferJob has one or more TransferJobItem(s).
 *
 * @param djStatus Status int as per TransferJobItemStatus
 *
 * @param djPubManifestUrl Where this TransferJob is being used to pin a publication, the manifest url,
 *        otherwise, null.
 */
@Entity
/**
 */
@Serializable
data class DownloadJob(
    @PrimaryKey(autoGenerate = true)
    val djUid: Int = 0,
    val djType: Int = 0,
    val djStatus: Int = 0,
    val djName: String? = null,
    val djPubManifestUrl: Url? = null,
    val djPubManifestHash: Long = 0,
    @ColumnInfo(defaultValue = "0")
    val djTimeCreated: Long = 0,
    @ColumnInfo(defaultValue = "0")
    val djCreationType: Int = 0,
) {
    companion object {

        const val TYPE_DOWNLOAD = 2

    }
}