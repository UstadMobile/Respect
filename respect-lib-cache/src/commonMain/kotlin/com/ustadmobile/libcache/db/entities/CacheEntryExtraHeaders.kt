package com.ustadmobile.libcache.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey


/**
 * Store extra headers that have been set using UstadCache.setExtraResponseHeaders
 */
@Entity
data class CacheEntryExtraHeaders(
    @PrimaryKey
    val ceehKey: String = "",

    val ceehUrl: String = "",

    val extraHeaders: String = "",
)