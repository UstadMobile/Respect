package com.ustadmobile.libcache.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ustadmobile.libcache.db.entities.CacheEntryExtraHeaders

@Dao
interface CacheEntryExtraHeadersDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertList(cacheEntryExtraHeaders: List<CacheEntryExtraHeaders>)

    @Query("""
        SELECT CacheEntryExtraHeaders.*
          FROM CacheEntryExtraHeaders
         WHERE CacheEntryExtraHeaders.ceehKey IN
               (SELECT RequestedEntry.requestedKey
                  FROM RequestedEntry
                 WHERE RequestedEntry.batchId = :batchId)
    """)
    suspend fun findByRequestBatchId(batchId: Int): List<CacheEntryExtraHeaders>

    @Query("""
        SELECT CacheEntryExtraHeaders.*
          FROM CacheEntryExtraHeaders
         WHERE CacheEntryExtraHeaders.ceehKey = :urlKey
    """)
    suspend fun findByKey(urlKey: String): CacheEntryExtraHeaders?

}