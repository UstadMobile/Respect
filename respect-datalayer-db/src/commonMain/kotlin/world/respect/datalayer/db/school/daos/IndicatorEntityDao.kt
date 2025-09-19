package world.respect.datalayer.db.school.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.db.school.entities.IndicatorEntity

@Dao
interface IndicatorEntityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun putIndicator(indicatorEntity: IndicatorEntity)

    @Query(
        """
        SELECT * 
        FROM IndicatorEntity
    """
    )
    fun getAllIndicator(): Flow<List<IndicatorEntity>>

    @Query(
        """
        SELECT * 
         FROM IndicatorEntity
        WHERE iGuidHash = :guidHash
    """
    )
    suspend fun findByGuidHash(guidHash: Long): IndicatorEntity?

    @Query(
        """
        SELECT * 
         FROM IndicatorEntity
        WHERE iGuidHash = :guidHash
    """
    )
    fun getIndicatorAsFlow(guidHash: Long): Flow<IndicatorEntity?>

    @Update
    suspend fun updateIndicator(entity: IndicatorEntity)

    @Query("SELECT COUNT(*) FROM IndicatorEntity")
    suspend fun getIndicatorCount(): Int
}