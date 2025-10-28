package world.respect.datalayer.db.school.daos.xapi

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import world.respect.datalayer.db.school.entities.xapi.ActivityEntity

@Dao
interface ActivityEntityDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnoreAsync(entities: List<ActivityEntity>)

    @Query(
        """
        UPDATE ActivityEntity
           SET actMoreInfo = :actMoreInfo,
               actLct = :actLct
        WHERE actUid = :activityUid
          AND actMoreInfo != :actMoreInfo      
    """
    )
    suspend fun updateIfMoreInfoChanged(
        activityUid: Long,
        actMoreInfo: String?,
        actLct: Long,
    )

    @Query(
        """
        UPDATE ActivityEntity
           SET actType = :actType,
               actMoreInfo = :actMoreInfo,
               actInteractionType = :actInteractionType,
               actCorrectResponsePatterns = :actCorrectResponsePatterns,
               actLct = :actLct
         WHERE actUid = :actUid
           AND (SELECT ActivityEntityInternal.actType 
                  FROM ActivityEntity ActivityEntityInternal 
                 WHERE ActivityEntityInternal.actUid = :actUid) IS NULL
           AND (SELECT ActivityEntityInternal.actInteractionType 
                  FROM ActivityEntity ActivityEntityInternal 
                 WHERE ActivityEntityInternal.actUid = :actUid) = ${ActivityEntity.TYPE_UNSET}
           AND (SELECT ActivityEntityInternal.actCorrectResponsePatterns 
                  FROM ActivityEntity ActivityEntityInternal 
                 WHERE ActivityEntityInternal.actUid = :actUid) IS NULL      
    """
    )
    suspend fun updateIfNotYetDefined(
        actUid: Long,
        actType: String?,
        actMoreInfo: String?,
        actInteractionType: Int,
        actCorrectResponsePatterns: String?,
        actLct: Long,
    )

    @Query(
        """
        SELECT ActivityEntity.*
          FROM ActivityEntity
         WHERE ActivityEntity.actUid = :activityUid 
    """
    )
    suspend fun findByUidAsync(activityUid: Long): ActivityEntity?
}