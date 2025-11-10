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

fun MIGRATION_2_3(
    deleteExisting: Boolean = false,
) = object: Migration(2, 3) {
    override fun migrate(connection: SQLiteConnection) {
        if(deleteExisting) {
            connection.execSQL("DELETE FROM PersonPasskeyEntity")
        }
        connection.execSQL("ALTER TABLE PersonPasskeyEntity RENAME TO PersonPasskeyEntity_old")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `PersonPasskeyEntity` (`ppPersonUid` INTEGER NOT NULL, `ppId` TEXT NOT NULL, `ppLastModified` INTEGER NOT NULL, `ppStored` INTEGER NOT NULL, `ppAttestationObj` TEXT, `ppClientDataJson` TEXT, `ppOriginString` TEXT, `ppChallengeString` TEXT, `ppPublicKey` TEXT, `isRevoked` INTEGER NOT NULL, PRIMARY KEY(`ppPersonUid`, `ppId`))")
        connection.execSQL("""
            INSERT INTO PersonPasskeyEntity (ppPersonUid, ppId, ppLastModified, ppStored, ppAttestationObj, ppClientDataJson, ppOriginString, ppChallengeString, ppPublicKey, isRevoked)
            SELECT ppPersonUid, ppId, ppLastModified, ppStored, ppAttestationObj, ppClientDataJson, ppOriginString, ppChallengeString, ppPublicKey, isRevoked
              FROM PersonPasskeyEntity_old
        """.trimIndent())
    }
}

val MIGRATE_3_4 = object: Migration(3, 4) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("""
            ALTER TABLE PersonPasskeyEntity 
             ADD COLUMN ppDeviceName TEXT NOT NULL DEFAULT ''
        """.trimIndent())
    }
}

val MIGRATE_4_5 = object: Migration(4, 5) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("""
            ALTER TABLE PersonPasskeyEntity 
             ADD COLUMN ppTimeCreated INTEGER NOT NULL DEFAULT 0
        """.trimIndent())
        connection.execSQL("""
            UPDATE PersonPasskeyEntity
               SET ppTimeCreated = ppLastModified
        """.trimIndent())
    }
}

val MIGRATE_5_6 = object: Migration(5, 6){
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            """
            ALTER TABLE PersonPasskeyEntity 
             ADD COLUMN ppAaguid TEXT NOT NULL DEFAULT ''
        """.trimIndent()
        )

        connection.execSQL(
            """
            ALTER TABLE PersonPasskeyEntity
            ADD COLUMN ppProviderName TEXT NOT NULL DEFAULT ''
        """.trimIndent()
        )
    }
}

val MIGRATE_6_7 = object: Migration(6, 7) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("""
            ALTER TABLE PersonPasskeyEntity
             ADD COLUMN ppIconLight TEXT NOT NULL DEFAULT ''
        """.trimIndent())

        connection.execSQL("""
            ALTER TABLE PersonPasskeyEntity
             ADD COLUMN ppIconDark TEXT NOT NULL DEFAULT ''
        """.trimIndent())
    }
}


val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("""
            CREATE TABLE `PersonRelatedPersonEntity_new` (
                `prpUid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `prpPersonUidNum` INTEGER NOT NULL,
                `prpOtherPersonUid` TEXT NOT NULL,
                `prpOtherPersonUidNum` INTEGER NOT NULL
            )
        """.trimIndent())

        connection.execSQL("""
            INSERT INTO `PersonRelatedPersonEntity_new` (prpPersonUidNum, prpOtherPersonUid, prpOtherPersonUidNum)
            SELECT prpPersonUidNum, prpOtherPersonUid, prpOtherPersonUidNum FROM `PersonRelatedPersonEntity`
        """.trimIndent())

        connection.execSQL("DROP TABLE `PersonRelatedPersonEntity`")
        connection.execSQL("ALTER TABLE `PersonRelatedPersonEntity_new` RENAME TO `PersonRelatedPersonEntity`")
    }
}


fun RoomDatabase.Builder<RespectSchoolDatabase>.addCommonMigrations(

): RoomDatabase.Builder<RespectSchoolDatabase> {
    return this.addMigrations(
        MIGRATION_1_2, MIGRATE_3_4, MIGRATE_4_5,MIGRATE_5_6, MIGRATE_6_7,MIGRATION_7_8
    )
}

