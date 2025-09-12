package world.respect.datalayer.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
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
import world.respect.datalayer.db.school.daos.IndicatorEntityDao
import world.respect.datalayer.db.school.daos.ReportEntityDao
import world.respect.datalayer.db.realm.entities.IndicatorEntity
import world.respect.datalayer.db.school.daos.ClazzEntityDao
import world.respect.datalayer.db.school.entities.ClassEntity
import world.respect.datalayer.db.school.entities.ReportEntity


/**
 * Contains realm-specific entities and DAOs
 */
@Database(
    entities = [
        PersonEntity::class,
        PersonRoleEntity::class,
        PersonPasswordEntity::class,
        AuthTokenEntity::class,
        ReportEntity::class,
        IndicatorEntity::class,
        ClassEntity::class,
    ],
    version = 1,

)
@TypeConverters(SharedConverters::class, SchoolTypeConverters::class)
@ConstructedBy(RespectSchoolDatabaseConstructor::class)
abstract class RespectSchoolDatabase: RoomDatabase() {

    abstract fun getPersonEntityDao(): PersonEntityDao

    abstract fun getPersonPasswordEntityDao(): PersonPasswordEntityDao

    abstract fun getAuthTokenEntityDao(): AuthTokenEntityDao

    abstract fun getPersonRoleEntityDao(): PersonRoleEntityDao

    abstract fun getReportEntityDao(): ReportEntityDao

    abstract fun getIndicatorEntityDao(): IndicatorEntityDao

    abstract fun getClassEntityDao(): ClazzEntityDao

}

// The Room compiler generates the `actual` implementations.
@Suppress("NO_ACTUAL_FOR_EXPECT", "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING",
    "KotlinNoActualForExpect", "RedundantSuppression"
)
expect object RespectSchoolDatabaseConstructor : RoomDatabaseConstructor<RespectSchoolDatabase> {
    override fun initialize(): RespectSchoolDatabase
}
