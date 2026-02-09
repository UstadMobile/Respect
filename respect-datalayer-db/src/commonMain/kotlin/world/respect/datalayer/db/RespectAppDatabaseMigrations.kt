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


fun RoomDatabase.Builder<RespectAppDatabase>.addCommonMigrations(

): RoomDatabase.Builder<RespectAppDatabase> {
    return this.addMigrations(
        APP_MIGRATION_2_3,
    )
}


