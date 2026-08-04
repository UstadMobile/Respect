package world.respect.datalayer.db

import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL


val APP_MIGRATION_2_3 = object: Migration(2,3) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS PersonPasskeyEntity")
        listOf(
            "LangMapEntity",
            "ReadiumLinkEntity",
            "OpdsPublicationEntity",
            "ReadiumSubjectEntity",
            "OpdsFacetEntity",
            "OpdsGroupEntity",
            "OpdsFeedEntity",
            "OpdsFeedMetadataEntity",
        ).forEach {
            connection.execSQL("DROP TABLE IF EXISTS $it")
        }
    }
}

val APP_MIGRATION_3_4 = object: Migration(3,4) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS CompatibleAppEntity")
        connection.execSQL("DROP TABLE IF EXISTS CompatibleAppAddJoin")
    }
}

val APP_MIGRATION_4_5 = object: Migration(4, 5) {

    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE SchoolDirectoryEntryEntity ADD COLUMN reInDirectoryUrl TEXT")
    }
}

val APP_MIGRATION_5_6 = object: Migration(5, 6) {

    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE SchoolDirectoryEntity ADD COLUMN rdName TEXT")
    }
}



fun RoomDatabase.Builder<RespectAppDatabase>.addCommonMigrations(

): RoomDatabase.Builder<RespectAppDatabase> {
    return this.addMigrations(
        APP_MIGRATION_2_3,
        APP_MIGRATION_3_4,
        APP_MIGRATION_4_5,
        APP_MIGRATION_5_6

    )
}


