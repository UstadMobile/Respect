package com.ustadmobile.libcache.db.migrations

import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import com.ustadmobile.libcache.db.UstadCacheDb

val MIGRATE_15_16 = object : Migration(15, 16) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `CacheEntryExtraHeaders` (`key` TEXT NOT NULL, `url` TEXT NOT NULL, `extraHeaders` TEXT NOT NULL, PRIMARY KEY(`key`))")
    }
}

fun RoomDatabase.Builder<UstadCacheDb>.addCacheDbMigrations(): RoomDatabase.Builder<UstadCacheDb> {
    return this.addMigrations(
        MIGRATE_15_16,
    )
}

