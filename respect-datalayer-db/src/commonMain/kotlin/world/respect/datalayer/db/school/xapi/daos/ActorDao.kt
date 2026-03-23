package world.respect.datalayer.db.school.xapi.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import world.respect.datalayer.db.school.xapi.entities.ActorEntity

@Dao
interface ActorDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnoreListAsync(entities: List<ActorEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertListAsync(entities: List<ActorEntity>)

    @Query(
        """
        UPDATE ActorEntity
           SET actorName = :name,
               actorLastModified = :updateTime
         WHERE actorUid = :uid
           AND ActorEntity.actorName != :name
    """
    )
    suspend fun updateIfNameChanged(
        uid: Long,
        name: String?,
        updateTime: Long,
    )

    @Query(
        """
        SELECT ActorEntity.*
          FROM ActorEntity
         WHERE ActorEntity.actorUid = :uid
    """
    )
    suspend fun findByUidAsync(uid: Long): ActorEntity?

    @Query(
        """
        SELECT ActorEntity.*
          FROM ActorEntity
         WHERE ActorEntity.actorUid = :actorUid
           AND ActorEntity.actorPersonUid = :accountPersonUid  
    """
    )
    suspend fun findByUidAndPersonUidAsync(
        actorUid: Long,
        accountPersonUid: Long,
    ): ActorEntity?

}