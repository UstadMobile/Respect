package com.ustadmobile.libcache.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import io.ktor.http.Url
import kotlinx.serialization.Serializable


/**
 * @param djiUrl: the source of the TransferJobItem - the value of the string depends on the type
 * @param djiDest: the destination of the TransferJobItem - the value of the string depends on the type
 */
@Entity(
    indices = [
        Index("djiDjUid", name="transferjob_djuid"),
    ]
)
@Serializable
data class DownloadJobItem(
    @PrimaryKey(autoGenerate = true)
    val djiUid: Int = 0,

    val djiDjUid: Int = 0,

    val djiTotalSize: Long = 0,

    val djiTransferred: Long = 0,

    val djiAttemptCount: Int = 0,

    val djiUrl: Url,

    val djiDest: String? = null,

    val djiType: Int = 0,

    val djiStatus: Int = 0,

    //This should be set when the transferjobitem is created - by query.
    @ColumnInfo(defaultValue = "0")
    val djiEntityEtag: Long = 0,

    @ColumnInfo(defaultValue = "0")
    val djiLockIdToRelease: Long = 0,

    val djiPartialTmpFile: String? = null,

)
