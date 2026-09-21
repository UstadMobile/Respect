package com.ustadmobile.libcache.db.entities

import androidx.room.Entity

/**
 * Represents a known entry in a neighbor cache
 *
 * @param nceNeighborUid the neighbor uid as per NeighborCache#neighborUid
 * @param nceUrlHash the xx64 hash of the URL
 */
@Entity(
    primaryKeys = arrayOf("nceNeighborUid", "nceUrlHash")
)
data class NeighborCacheEntry(
    val nceNeighborUid: Long = 0L,
    val nceUrlHash: Long = 0L,
)
