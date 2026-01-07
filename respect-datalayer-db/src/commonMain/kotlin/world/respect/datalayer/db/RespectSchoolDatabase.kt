package world.respect.datalayer.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
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
import world.respect.datalayer.db.opds.entities.ReadiumLinkEntity
import world.respect.datalayer.db.opds.entities.ReadiumSubjectEntity
import world.respect.datalayer.db.school.SchoolTypeConverters
import world.respect.datalayer.db.school.daos.AuthTokenEntityDao
import world.respect.datalayer.db.school.daos.PersonEntityDao
import world.respect.datalayer.db.school.daos.PersonPasswordEntityDao
import world.respect.datalayer.db.school.daos.PersonRoleEntityDao
import world.respect.datalayer.db.school.entities.AuthTokenEntity
import world.respect.datalayer.db.school.entities.PersonEntity
import world.respect.datalayer.db.school.entities.PersonPasswordEntity
import world.respect.datalayer.db.school.entities.PersonRoleEntity
import world.respect.datalayer.db.shared.SharedConverters
import world.respect.datalayer.db.shared.daos.LangMapEntityDao
import world.respect.datalayer.db.shared.entities.LangMapEntity
import world.respect.datalayer.db.school.daos.IndicatorEntityDao
import world.respect.datalayer.db.school.daos.ReportEntityDao
import world.respect.datalayer.db.realm.entities.IndicatorEntity
import world.respect.datalayer.db.school.daos.AssignmentEntityDao
import world.respect.datalayer.db.school.daos.AssignmentLearningResourceRefEntityDao
import world.respect.datalayer.db.school.daos.ClassEntityDao
import world.respect.datalayer.db.school.daos.ClassPermissionEntityDao
import world.respect.datalayer.db.school.daos.EnrollmentEntityDao
import world.respect.datalayer.db.school.daos.PersonRelatedPersonEntityDao
import world.respect.datalayer.db.school.daos.PullSyncStatusEntityDao
import world.respect.datalayer.db.school.daos.SchoolAppEntityDao
import world.respect.datalayer.db.school.daos.WriteQueueItemEntityDao
import world.respect.datalayer.db.school.entities.AssignmentEntity
import world.respect.datalayer.db.school.entities.AssignmentLearningResourceRefEntity
import world.respect.datalayer.db.school.entities.ClassEntity
import world.respect.datalayer.db.school.entities.EnrollmentEntity
import world.respect.datalayer.db.school.entities.PersonPasskeyEntity
import world.respect.datalayer.db.school.entities.PersonRelatedPersonEntity
import world.respect.datalayer.db.school.entities.ReportEntity
import world.respect.datalayer.db.school.entities.SchoolAppEntity
import world.respect.datalayer.db.school.entities.WriteQueueItemEntity
import world.respect.datalayer.db.school.daos.SchoolPermissionGrantDao
import world.respect.datalayer.db.school.entities.ClassPermissionEntity
import world.respect.datalayer.db.school.entities.PullSyncStatusEntity
import world.respect.datalayer.db.school.entities.SchoolPermissionGrantEntity
import world.respect.datalayer.school.model.Assignment
import world.respect.datalayer.school.model.Clazz
import world.respect.datalayer.school.model.Enrollment
import world.respect.datalayer.school.model.Indicator
import world.respect.datalayer.school.model.Person
import world.respect.datalayer.school.model.Report


/**
 * Contains realm-specific entities and DAOs
 */
@Database(
    entities = [
        SchoolAppEntity::class,
        PersonEntity::class,
        PersonRoleEntity::class,
        PersonRelatedPersonEntity::class,
        PersonPasswordEntity::class,
        AuthTokenEntity::class,
        ReportEntity::class,
        IndicatorEntity::class,
        ClassEntity::class,
        ClassPermissionEntity::class,
        EnrollmentEntity::class,
        AssignmentEntity::class,
        AssignmentLearningResourceRefEntity::class,
        WriteQueueItemEntity::class,
        SchoolPermissionGrantEntity::class,
        PullSyncStatusEntity::class,

        //Shared (used by OPDS)
        LangMapEntity::class,

        //OPDS
        ReadiumLinkEntity::class,
        OpdsPublicationEntity::class,
        ReadiumSubjectEntity::class,
        OpdsFacetEntity::class,
        OpdsGroupEntity::class,
        OpdsFeedEntity::class,
        OpdsFeedMetadataEntity::class,

        // PersonPasskeyEntity exists in both db and opds.daos. It is specific to 'school', so I'll keep it under school and remove the opds.daos import.
        // It seems there's a duplication in the import list for PersonPasskeyEntityDao from opds.daos and school.daos.
        // I'll keep the one from school.daos as it seems to be the intended one for SchoolDatabase.
        PersonPasskeyEntity::class,
    ],
    version = 10,
)
@TypeConverters(SharedConverters::class, SchoolTypeConverters::class, OpdsTypeConverters::class)
@ConstructedBy(RespectSchoolDatabaseConstructor::class)
abstract class RespectSchoolDatabase: RoomDatabase() {

    abstract fun getSchoolAppEntityDao(): SchoolAppEntityDao

    abstract fun getPersonEntityDao(): PersonEntityDao

    abstract fun getPersonPasswordEntityDao(): PersonPasswordEntityDao

    abstract fun getPersonPasskeyEntityDao(): PersonPasskeyEntityDao

    abstract fun getAuthTokenEntityDao(): AuthTokenEntityDao

    abstract fun getPersonRoleEntityDao(): PersonRoleEntityDao

    abstract fun getPersonRelatedPersonEntityDao(): PersonRelatedPersonEntityDao

    abstract fun getReportEntityDao(): ReportEntityDao

    abstract fun getIndicatorEntityDao(): IndicatorEntityDao

    abstract fun getClassEntityDao(): ClassEntityDao

    abstract fun getClassPermissionEntityDao(): ClassPermissionEntityDao

    abstract fun getEnrollmentEntityDao(): EnrollmentEntityDao

    abstract fun getAssignmentEntityDao(): AssignmentEntityDao

    abstract fun getAssignmentLearningResourceRefEntityDao(): AssignmentLearningResourceRefEntityDao

    abstract fun getWriteQueueItemEntityDao(): WriteQueueItemEntityDao

    abstract fun getSchoolPermissionGrantDao(): SchoolPermissionGrantDao

    abstract fun getPullSyncStatusEntityDao(): PullSyncStatusEntityDao

    abstract fun getLangMapEntityDao(): LangMapEntityDao

    abstract fun getOpdsFeedEntityDao(): OpdsFeedEntityDao

    abstract fun getOpdsPublicationEntityDao(): OpdsPublicationEntityDao

    abstract fun getOpdsFeedMetadataEntityDao(): OpdsFeedMetadataEntityDao

    abstract fun getReadiumLinkEntityDao(): ReadiumLinkEntityDao

    abstract fun getOpdsGroupEntityDao(): OpdsGroupEntityDao


    companion object {

        val TABLE_IDS = listOf(
            Person.TABLE_ID,
            Report.TABLE_ID,
            Indicator.TABLE_ID,
            Enrollment.TABLE_ID,
            Clazz.TABLE_ID,
            PersonPasskeyEntity.TABLE_ID,
            Assignment.TABLE_ID,
            ReadiumLinkEntity.TABLE_ID,
            OpdsPublicationEntity.TABLE_ID,
            OpdsFacetEntity.TABLE_ID,
            OpdsGroupEntity.TABLE_ID,
            OpdsFeedEntity.TABLE_ID,
        )

    }
}

// The Room compiler generates the `actual` implementations.
@Suppress("NO_ACTUAL_FOR_EXPECT", "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING",
    "KotlinNoActualForExpect", "RedundantSuppression"
)
expect object RespectSchoolDatabaseConstructor : RoomDatabaseConstructor<RespectSchoolDatabase> {
    override fun initialize(): RespectSchoolDatabase
}
