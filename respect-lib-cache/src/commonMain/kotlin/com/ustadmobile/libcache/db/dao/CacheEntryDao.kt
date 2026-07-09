package com.ustadmobile.libcache.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.libcache.db.entities.CacheEntry

@Dao
abstract class CacheEntryDao {

    @Query("""
        SELECT CacheEntry.*
          FROM CacheEntry
         WHERE CacheEntry.url = :url 
    """)
    abstract suspend fun findByUrlAsync(url: String): CacheEntry?

    @Query("""
        SELECT CacheEntry.*
          FROM CacheEntry
         WHERE CacheEntry.`key` = :key
    """)
    abstract suspend fun findEntryByKey(
        key: String,
    ): CacheEntry?

    @Query("""
        SELECT CacheEntry.`key`
          FROM CacheEntry
         WHERE CacheEntry.urlWithoutSearchHash = :urlHash 
    """)
    abstract suspend fun findKeysByUrlWithoutSearchHash(
        urlHash: String
    ): List<String>

    @Insert
    abstract suspend fun insertAsync(entry: CacheEntry): Long

    @Insert
    abstract suspend fun insertList(entry: List<CacheEntry>)

    @Update
    abstract suspend fun updateList(entry: List<CacheEntry>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertList(entry: List<CacheEntry>)

    @Query("""
        SELECT RequestedEntry.requestedKey
          FROM RequestedEntry
         WHERE RequestedEntry.batchId = :batchId
           AND EXISTS(
               SELECT RetentionLock.lockId
                 FROM RetentionLock
                WHERE RetentionLock.lockKey = RequestedEntry.requestedKey)
    """)
    abstract suspend fun findEntriesWithLock(batchId: Int): List<String>



    @Query("""
        UPDATE CacheEntry
           SET lastAccessed = :lastAccessTime
         WHERE key = :key  
    """)
    abstract suspend fun updateLastAccessedTime(key: String, lastAccessTime: Long)

    /**
     * Find entries that can be evicted e.g. entries for which there is no RetentionLock
     */
    @Query("""
        SELECT CacheEntry.*
          FROM CacheEntry
         WHERE NOT EXISTS(
               SELECT RetentionLock.lockId
                 FROM RetentionLock
                WHERE RetentionLock.lockKey = CacheEntry.key) 
      ORDER BY lastAccessed ASC           
         LIMIT :batchSize       
      
    """)
    abstract suspend fun findEvictableEntries(batchSize: Int): List<CacheEntry>

    /**
     * Get the total size of evictable entries.
     */
    @Query("""
        SELECT SUM(CacheEntry.storageSize)
          FROM CacheEntry
         WHERE NOT EXISTS(
               SELECT RetentionLock.lockId
                 FROM RetentionLock
                WHERE RetentionLock.lockKey = CacheEntry.key)  
    """)
    abstract suspend fun totalEvictableSize(): Long

    @Delete
    abstract suspend fun delete(entries: List<CacheEntry>)

    @Query("""
        UPDATE CacheEntry
           SET responseHeaders = :headers,
               lastValidated = :lastValidated,
               lastAccessed = :lastAccessed
         WHERE key = :key      
    """)
    abstract suspend  fun updateValidation(
        key: String,
        headers: String,
        lastValidated: Long,
        lastAccessed: Long,
    )

    @Query("""
        SELECT CacheEntry.url
          FROM CacheEntry
      ORDER BY CacheEntry.key 
         LIMIT :limit
        OFFSET :offset
       
    """)
    abstract suspend fun getEntryUrlsInOrder(offset: Int, limit: Int): List<String>

}