package world.respect.datasource.db.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import world.respect.datasource.db.entities.CompatibleAppEntity

@Dao
interface CompatibleAppEntityDao {

    @Query("SELECT * FROM CompatibleAppEntity")
    fun selectAllAsFlow(): Flow<List<CompatibleAppEntity>>

    @Insert(onConflict = REPLACE)
    suspend fun upsert(compatibleApps: List<CompatibleAppEntity>)

}