package com.ustadmobile.libcache.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class PinnedPublication(
    @PrimaryKey
    val ppUrlHash: Long,
    val title: String,
)