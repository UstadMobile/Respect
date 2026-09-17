package com.ustadmobile.libcache.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Used to make things more efficient when working with batch requests.
 */
@Entity
data class RequestedEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val requestSha256: String = "",
    val requestedKey: String = "",
    @ColumnInfo(index = true)
    val batchId: Int = 0,
)