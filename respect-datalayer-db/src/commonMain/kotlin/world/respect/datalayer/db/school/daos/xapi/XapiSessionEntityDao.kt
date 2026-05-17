package world.respect.datalayer.db.school.daos.xapi

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import world.respect.datalayer.db.school.entities.xapi.XapiSessionEntity

@Dao
interface XapiSessionEntityDao {

    @Insert
    suspend fun insertAsync(xapiSessionEntity: XapiSessionEntity)

    @Query(
        """
        SELECT XapiSessionEntity.*
          FROM XapiSessionEntity
         WHERE XapiSessionEntity.xseUid = :uid
    """
    )
    suspend fun findByUidAsync(uid: Long): XapiSessionEntity?

    @Query(
        """
        UPDATE XapiSessionEntity
           SET xseCompleted = :completed,
               xseLastMod = :time
         WHERE xseUid = :xseUid

    """
    )
    suspend fun updateLatestAsComplete(
        completed: Boolean,
        time: Long,
        xseUid: Long,
    )

    @Query(
        """
        SELECT XapiSessionEntity.*
          FROM XapiSessionEntity
         WHERE XapiSessionEntity.xseRootActivityUid = :xseRootActivityUid
           AND XapiSessionEntity.xseActorUid = :actorUid
           AND XapiSessionEntity.xseContentEntryVersionUid = :contentEntryVersionUid
           AND XapiSessionEntity.xseClazzUid = :clazzUid
           AND EXISTS(
               SELECT 1
                 FROM ActorEntity
                WHERE ActorEntity.actorUid = :actorUid
                  AND ActorEntity.actorPersonUid = :accountPersonUid)     
    """
    )
    suspend fun findMostRecentSessionByActorAndActivity(
        accountPersonUid: Long,
        actorUid: Long,
        xseRootActivityUid: Long,
        contentEntryVersionUid: Long,
        clazzUid: Long,
    ): XapiSessionEntity?
}