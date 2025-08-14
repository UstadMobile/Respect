package world.respect.datalayer.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import world.respect.datalayer.db.compatibleapps.daos.CompatibleAppAddJoinDao
import world.respect.datalayer.db.compatibleapps.daos.CompatibleAppEntityDao
import world.respect.datalayer.db.compatibleapps.entities.CompatibleAppAddJoin
import world.respect.datalayer.db.compatibleapps.entities.CompatibleAppEntity
import world.respect.datalayer.db.opds.OpdsTypeConverters
import world.respect.datalayer.db.opds.daos.OpdsFeedEntityDao
import world.respect.datalayer.db.opds.daos.OpdsFeedMetadataEntityDao
import world.respect.datalayer.db.opds.daos.OpdsGroupEntityDao
import world.respect.datalayer.db.opds.daos.OpdsPublicationEntityDao
import world.respect.datalayer.db.opds.daos.PersonPasskeyEntityDao
import world.respect.datalayer.db.opds.daos.ReadiumLinkEntityDao
import world.respect.datalayer.db.opds.entities.OpdsFacetEntity
import world.respect.datalayer.db.opds.entities.OpdsFeedEntity
import world.respect.datalayer.db.opds.entities.OpdsFeedMetadataEntity
import world.respect.datalayer.db.opds.entities.OpdsGroupEntity
import world.respect.datalayer.db.opds.entities.OpdsPublicationEntity
import world.respect.datalayer.db.opds.entities.PersonPasskeyEntity
import world.respect.datalayer.db.opds.entities.ReadiumLinkEntity
import world.respect.datalayer.db.opds.entities.ReadiumSubjectEntity
import world.respect.datalayer.db.shared.SharedConverters
import world.respect.datalayer.db.shared.daos.LangMapEntityDao
import world.respect.datalayer.db.shared.entities.LangMapEntity

@Database(
    entities = [
        CompatibleAppEntity::class,
        LangMapEntity::class,
        ReadiumLinkEntity::class,
        OpdsPublicationEntity::class,
        ReadiumSubjectEntity::class,
        OpdsFacetEntity::class,
        OpdsGroupEntity::class,
        OpdsFeedEntity::class,
        OpdsFeedMetadataEntity::class,
        CompatibleAppAddJoin::class,
        PersonPasskeyEntity::class,
    ],
    version = 1,
)
@TypeConverters(SharedConverters::class, OpdsTypeConverters::class)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class RespectDatabase: RoomDatabase() {

    abstract fun getCompatibleAppEntityDao(): CompatibleAppEntityDao

    abstract fun getCompatibleAppAddJoinDao(): CompatibleAppAddJoinDao

    abstract fun getLangMapEntityDao(): LangMapEntityDao

    abstract fun getOpdsFeedEntityDao(): OpdsFeedEntityDao

    abstract fun getOpdsPublicationEntityDao(): OpdsPublicationEntityDao

    abstract fun getOpdsFeedMetadataEntityDao(): OpdsFeedMetadataEntityDao

    abstract fun getReadiumLinkEntityDao(): ReadiumLinkEntityDao

    abstract fun getOpdsGroupEntityDao(): OpdsGroupEntityDao

    abstract fun getPersonPasskeyDao(): PersonPasskeyEntityDao

    companion object {

        val TABLE_IDS = listOf(
            ReadiumLinkEntity.TABLE_ID,
            OpdsPublicationEntity.TABLE_ID,
            OpdsFacetEntity.TABLE_ID,
            OpdsGroupEntity.TABLE_ID,
            OpdsFeedEntity.TABLE_ID,
            PersonPasskeyEntity.TABLE_ID,
        )

    }
}

// The Room compiler generates the `actual` implementations.
@Suppress("NO_ACTUAL_FOR_EXPECT", "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING",
    "KotlinNoActualForExpect"
)
expect object AppDatabaseConstructor : RoomDatabaseConstructor<RespectDatabase> {
    override fun initialize(): RespectDatabase
}
