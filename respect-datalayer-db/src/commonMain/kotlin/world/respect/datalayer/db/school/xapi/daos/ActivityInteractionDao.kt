package world.respect.datalayer.db.school.xapi.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import world.respect.datalayer.db.school.xapi.entities.ActivityInteractionEntity

@Dao
interface ActivityInteractionDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnoreAsync(entities: List<ActivityInteractionEntity>)


    @Query("""
        DELETE FROM ActivityInteractionEntity 
         WHERE aieActivityUid = :activityUid
    """)
    suspend fun deleteByActivityUid(activityUid: Long)

    @Query("""
        SELECT ActivityInteractionEntity.*
          FROM ActivityInteractionEntity
         WHERE ActivityInteractionEntity.aieActivityUid = :activityUid 
    """)
    suspend fun findAllByActivityUidAsync(
        activityUid: Long
    ): List<ActivityInteractionEntity>



}