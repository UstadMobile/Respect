package world.respect.datalayer.db.school.xapi.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import world.respect.datalayer.db.school.xapi.entities.ActivityExtensionEntity

@Dao
interface ActivityExtensionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertListAsync(list: List<ActivityExtensionEntity>)

    @Query("""
        SELECT ActivityExtensionEntity.*
          FROM ActivityExtensionEntity
         WHERE ActivityExtensionEntity.aeeActivityUid = :activityUid 
    """)
    suspend fun findAllByActivityUid(activityUid: Long): List<ActivityExtensionEntity>


}