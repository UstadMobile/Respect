package com.ustadmobile.libcache.db.migrations

import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import com.ustadmobile.libcache.db.UstadCacheDb

val MIGRATE_15_16 = object : Migration(15, 16) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE CacheEntry ADD COLUMN keyWithoutSearch TEXT")
        connection.execSQL("ALTER TABLE CacheEntry ADD COLUMN urlWithoutSearch TEXT")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `idx_urlWithoutSearchHash` ON `CacheEntry` (`urlWithoutSearchHash`)")
    }
}

val MIGRATE_16_17 = object : Migration(16, 17) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `CacheEntryExtraHeaders` (`ceehKey` TEXT NOT NULL, `ceehUrl` TEXT NOT NULL, `extraHeaders` TEXT NOT NULL, PRIMARY KEY(`ceehKey`))")
    }
}

fun RoomDatabase.Builder<UstadCacheDb>.addCacheDbMigrations(): RoomDatabase.Builder<UstadCacheDb> {
    return this.addMigrations(
        MIGRATE_15_16,
        MIGRATE_16_17,
    )
}

