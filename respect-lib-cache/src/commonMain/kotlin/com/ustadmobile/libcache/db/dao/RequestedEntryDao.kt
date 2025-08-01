package com.ustadmobile.libcache.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ustadmobile.libcache.db.entities.RequestedEntry

@Dao
abstract class RequestedEntryDao {

    @Insert
    abstract suspend fun insertList(requestedSha256s: List<RequestedEntry>)

    @Insert
    abstract suspend fun insertListAsync(requestedSha256s: List<RequestedEntry>)

    @Query(
        """
        SELECT RequestedEntry.requestedKey
          FROM RequestedEntry
         WHERE RequestedEntry.batchId = :batchId 
           AND NOT EXISTS(
               SELECT CacheEntry.key
                 FROM CacheEntry
                WHERE CacheEntry.key = RequestedEntry.requestedKey
           )
        """
    )
    abstract suspend fun findKeysNotPresent(
        batchId: Int,
    ): List<String>

    @Query("""
        DELETE FROM RequestedEntry
         WHERE RequestedEntry.batchId = :batchId    
    """)
    abstract suspend fun deleteBatch(batchId: Int)

}