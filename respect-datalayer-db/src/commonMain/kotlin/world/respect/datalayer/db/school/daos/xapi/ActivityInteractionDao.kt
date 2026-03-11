package world.respect.datalayer.db.school.daos.xapi

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import world.respect.datalayer.db.school.entities.xapi.ActivityInteractionEntity

@Dao
interface ActivityInteractionDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnoreAsync(entities: List<ActivityInteractionEntity>)

    @Query(
        """
        SELECT DISTINCT ActivityInteractionEntity.aieActivityUid
          FROM ActivityInteractionEntity
         WHERE ActivityInteractionEntity.aieActivityUid IN (:activityUids)
    """
    )
    suspend fun findActivityUidsWithInteractionEntitiesAsync(
        activityUids: List<Long>
    ): List<Long>

    @Query(
        """
        SELECT ActivityInteractionEntity.*
          FROM ActivityInteractionEntity
         WHERE ActivityInteractionEntity.aieActivityUid = :activityUid 
    """
    )
    suspend fun findAllByActivityUidAsync(
        activityUid: Long
    ): List<ActivityInteractionEntity>


}