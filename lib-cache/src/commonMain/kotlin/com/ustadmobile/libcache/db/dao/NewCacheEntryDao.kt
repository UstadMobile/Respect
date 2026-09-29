package com.ustadmobile.libcache.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.ustadmobile.libcache.db.entities.NewCacheEntry

@Dao
abstract class NewCacheEntryDao {

    @Query("""
        SELECT NewCacheEntry.*
          FROM NewCacheEntry
    """)
    abstract suspend fun findAllNewEntries(): List<NewCacheEntry>

    @Query("""DELETE FROM NewCacheEntry""")
    abstract suspend fun clearAll()

}