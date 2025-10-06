package world.respect.datalayer.db

import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

val APP_MIGRATION_1_2 = object: Migration(1,2) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS PersonPasskeyEntity")
    }
}

fun RoomDatabase.Builder<RespectAppDatabase>.addCommonMigrations(

): RoomDatabase.Builder<RespectAppDatabase> {
    return this.addMigrations(
        APP_MIGRATION_1_2,
    )
}


