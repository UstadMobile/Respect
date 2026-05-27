package world.respect.datalayer.db.school.xapi.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import world.respect.datalayer.db.school.xapi.entities.XapiActivityExtensionEntity

@Dao
interface XapiActivityExtensionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertListAsync(list: List<XapiActivityExtensionEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnore(list: List<XapiActivityExtensionEntity>)

    @Query(
        """
        DELETE FROM XapiActivityExtensionEntity 
              WHERE aeeActivityUid = :activityUid
    """
    )
    suspend fun deleteByActivityUid(activityUid: Long)

    @Query(
        """
        SELECT XapiActivityExtensionEntity.*
          FROM XapiActivityExtensionEntity
         WHERE XapiActivityExtensionEntity.aeeActivityUid = :activityUid 
    """
    )
    suspend fun findAllByActivityUid(activityUid: Long): List<XapiActivityExtensionEntity>

    @Query("""
        UPDATE XapiActivityExtensionEntity
           SET aeeJson = :json, 
               aeeLastMod = :changeTime
         WHERE aeeActivityUid = :activityUid 
           AND aeeKeyHash = :keyHash
           AND aeeLastMod < :changeTime
    """)
    suspend fun updateIfNewer(
        activityUid: Long,
        keyHash: Long,
        json: String,
        changeTime: Long
    )


}