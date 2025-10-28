package world.respect.datalayer.db.school.daos.xapi

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import world.respect.datalayer.db.school.composite.ActorUidEtagAndLastMod
import world.respect.datalayer.db.school.entities.xapi.ActorEntity

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
               actorLct = :updateTime
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


    @Query(
        """
        SELECT ActorEntity.actorUid, ActorEntity.actorEtag, ActorEntity.actorLct
          FROM ActorEntity
         WHERE ActorEntity.actorUid IN (:uidList)
    """
    )
    suspend fun findUidAndEtagByListAsync(uidList: List<Long>): List<ActorUidEtagAndLastMod>

    @Query(
        """
        SELECT ActorEntity.*
          FROM ActorEntity
         WHERE ActorEntity.actorUid IN (
               SELECT GroupMemberActorJoin.gmajMemberActorUid
                 FROM GroupMemberActorJoin
                WHERE GroupMemberActorJoin.gmajGroupActorUid = :groupActorUid
                  AND GroupMemberActorJoin.gmajLastMod = (
                      SELECT GroupActorEntity.actorLct
                        FROM ActorEntity GroupActorEntity
                       WHERE GroupActorEntity.actorUid = :groupActorUid)
              ) 
    """
    )
    suspend fun findGroupMembers(groupActorUid: Long): List<ActorEntity>
}