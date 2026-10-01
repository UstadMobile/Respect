package com.ustadmobile.libcache.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ustadmobile.libcache.db.composites.NeighborCacheEntryAndNeighborCache
import com.ustadmobile.libcache.db.entities.NeighborCache
import com.ustadmobile.libcache.db.entities.NeighborCacheEntry
import kotlinx.coroutines.flow.Flow

@Dao
abstract class NeighborCacheEntryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertList(neighborCacheEntryList: List<NeighborCacheEntry>)

    @Query("""
        SELECT NeighborCacheEntry.* 
          FROM NeighborCacheEntry
    """)
    abstract fun allEntriesAsFlow(): Flow<List<NeighborCacheEntry>>


    @Query("""
        SELECT NeighborCacheEntry.nceUrlHash
          FROM NeighborCacheEntry
         WHERE NeighborCacheEntry.nceUrlHash IN (:urlHashes) 
    """)
    abstract suspend fun findAvailableEntries(
        urlHashes: List<Long>
    ): List<Long>

    @Query("""
        SELECT NeighborCacheEntry.*,
               NeighborCache.*
          FROM NeighborCacheEntry
               JOIN NeighborCache
                    ON NeighborCache.neighborUid = NeighborCacheEntry.nceNeighborUid
         WHERE NeighborCacheEntry.nceUrlHash = :urlHash
           AND NeighborCache.neighborStatus = ${NeighborCache.STATUS_ACTIVE}
    """)
    abstract suspend fun findAvailableNeighborsByUrlHash(
        urlHash: Long
    ): List<NeighborCacheEntryAndNeighborCache>

}