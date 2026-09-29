package com.ustadmobile.libcache.db

import androidx.room.RoomDatabase
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * When a new entry is added to the Cache the DistributedCacheHashTable needs to inform neighbors.
 *
 * The generated trigger allows DistributedCacheHashTable to simply monitor the NewCacheEntry table
 */
class AddNewEntryTriggerCallback: RoomDatabase.Callback() {

    override fun onCreate(connection: SQLiteConnection) {
        connection.execSQL(ADD_TRIGGER_SQL)
    }

    companion object {

        const val ADD_TRIGGER_SQL = """
            CREATE TRIGGER NewCacheEntryTrigger 
            AFTER INSERT ON CacheEntry
            BEGIN
               INSERT OR REPLACE INTO NewCacheEntry(cacheEntryKey, nceUrl) VALUES(NEW.key, NEW.url);
            END
        """

    }
}