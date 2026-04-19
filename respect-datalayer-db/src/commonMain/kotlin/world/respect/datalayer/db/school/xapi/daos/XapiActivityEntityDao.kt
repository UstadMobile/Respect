package world.respect.datalayer.db.school.xapi.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import world.respect.datalayer.db.school.xapi.adapters.XapiActivityEntities
import world.respect.datalayer.db.school.xapi.entities.XapiActivityEntity

@Dao
interface XapiActivityEntityDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnoreAsync(entities: List<XapiActivityEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(xapiActivityEntity: XapiActivityEntity)



    @Query(
        """
        UPDATE XapiActivityEntity
           SET actMoreInfo = :actMoreInfo,
               actNonSignificantLastModified = :actLct
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
        UPDATE XapiActivityEntity
           SET actType = :actType,
               actMoreInfo = :actMoreInfo,
               actInteractionType = :actInteractionType,
               actCorrectResponsePatterns = :actCorrectResponsePatterns,
               actNonSignificantLastModified = :actLct
         WHERE actUid = :actUid
           AND (SELECT ActivityEntityInternal.actType 
                  FROM XapiActivityEntity ActivityEntityInternal 
                 WHERE ActivityEntityInternal.actUid = :actUid) IS NULL
           AND (SELECT ActivityEntityInternal.actInteractionType 
                  FROM XapiActivityEntity ActivityEntityInternal 
                 WHERE ActivityEntityInternal.actUid = :actUid) = ${XapiActivityEntity.TYPE_UNSET}
           AND (SELECT ActivityEntityInternal.actCorrectResponsePatterns 
                  FROM XapiActivityEntity ActivityEntityInternal 
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
        SELECT XapiActivityEntity.*
          FROM XapiActivityEntity
         WHERE XapiActivityEntity.actUid = :activityUid 
    """
    )
    suspend fun findByUidAsync(activityUid: Long): XapiActivityEntity?

    @Query(
        """
        SELECT XapiActivityEntity.*
          FROM XapiActivityEntity
         WHERE XapiActivityEntity.actUid = :activityUid 
    """
    )
    suspend fun getEntitiesByUid(activityUid: Long): XapiActivityEntities?


}