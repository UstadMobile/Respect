package com.ustadmobile.libcache.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ustadmobile.libcache.db.entities.RetentionLock

@Dao
abstract class RetentionLockDao {

    @Insert
    abstract suspend fun insert(retentionLock: RetentionLock): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertList(retentionLocks: List<RetentionLock>)

    @Delete
    abstract suspend fun delete(retentionLocks: List<RetentionLock>)

    @Query("""
        SELECT RetentionLock.*
          FROM RetentionLock
         WHERE RetentionLock.lockKey IN 
               (SELECT RequestedEntry.requestedKey
                  FROM RequestedEntry
                 WHERE RequestedEntry.batchId = :batchId)
    """)
    abstract suspend fun findByBatchId(batchId: Int): List<RetentionLock>

    @Query("""
        SELECT RetentionLock.*
          FROM RetentionLock
         WHERE RetentionLock.lockKey = :urlKey 
    """)
    abstract suspend fun findByKey(urlKey: String): List<RetentionLock>

    @Query("""
        SELECT RetentionLock.*
          FROM RetentionLock
         WHERE RetentionLock.lockPublicationUid = :publicationUid
    """)
    abstract suspend fun findByPublicationUid(publicationUid: Long): List<RetentionLock>

}