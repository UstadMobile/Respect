package world.respect.datasource.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import world.respect.datasource.db.daos.CompatibleAppEntityDao
import world.respect.datasource.db.daos.LangMapEntityDao
import world.respect.datasource.db.entities.CompatibleAppEntity
import world.respect.datasource.db.entities.LangMapEntity


@Database(
    entities = [CompatibleAppEntity::class, LangMapEntity::class],
    version = 1
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class RespectDatabase: RoomDatabase() {

    abstract fun getCompatibleAppEntityDao(): CompatibleAppEntityDao

    abstract fun getLangMapEntityDao(): LangMapEntityDao

}

// The Room compiler generates the `actual` implementations.
@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<RespectDatabase> {
    override fun initialize(): RespectDatabase
}
