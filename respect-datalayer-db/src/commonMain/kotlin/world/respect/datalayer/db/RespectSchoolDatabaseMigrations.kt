package world.respect.datalayer.db

import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

val MIGRATION_1_2 = object: Migration(1, 2) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("""
            ALTER TABLE PersonPasskeyEntity
            ADD COLUMN ppLastModified INTEGER NOT NULL DEFAULT 0
        """)

        connection.execSQL("""
            ALTER TABLE PersonPasskeyEntity
            ADD COLUMN ppStored INTEGER NOT NULL DEFAULT 0
        """)
    }
}

fun RoomDatabase.Builder<RespectSchoolDatabase>.addCommonMigrations(

): RoomDatabase.Builder<RespectSchoolDatabase> {
    return this.addMigrations(
        MIGRATION_1_2,
    )
}

