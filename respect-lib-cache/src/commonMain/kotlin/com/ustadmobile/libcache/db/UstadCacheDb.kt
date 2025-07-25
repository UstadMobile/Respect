package com.ustadmobile.libcache.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.ustadmobile.libcache.db.dao.CacheEntryDao
import com.ustadmobile.libcache.db.dao.RequestedEntryDao
import com.ustadmobile.libcache.db.dao.RetentionLockDao
import com.ustadmobile.libcache.db.dao.NeighborCacheDao
import com.ustadmobile.libcache.db.dao.NeighborCacheEntryDao
import com.ustadmobile.libcache.db.dao.NewCacheEntryDao
import com.ustadmobile.libcache.db.entities.CacheEntry
import com.ustadmobile.libcache.db.entities.NeighborCache
import com.ustadmobile.libcache.db.entities.NeighborCacheEntry
import com.ustadmobile.libcache.db.entities.NewCacheEntry
import com.ustadmobile.libcache.db.entities.RequestedEntry
import com.ustadmobile.libcache.db.entities.RetentionLock

/**
 * CacheEntry
 *  url, headers, etc.
 *
 * ResponseData
 *  sha256 storageUri
 *
 * RetentionLock
 *   LockId
 *   EntryId
 *
 */
@Database(
    version = 15,
    entities = [
        CacheEntry::class,
        RequestedEntry::class,
        RetentionLock::class,
        NeighborCache::class,
        NeighborCacheEntry::class,
        NewCacheEntry::class
    ],
)
abstract class UstadCacheDb : RoomDatabase() {

    abstract val cacheEntryDao: CacheEntryDao

    abstract val requestedEntryDao: RequestedEntryDao

    abstract val retentionLockDao: RetentionLockDao

    abstract val neighborCacheDao: NeighborCacheDao

    abstract val neighborCacheEntryDao: NeighborCacheEntryDao

    abstract val newCacheEntryDao: NewCacheEntryDao

}

// The Room compiler generates the `actual` implementations.
@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<UstadCacheDb> {
    override fun initialize(): UstadCacheDb
}

