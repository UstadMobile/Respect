package world.respect.datalayer.db.school.daos.xapi

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import world.respect.datalayer.db.school.composite.StateIdAndLastModified
import world.respect.datalayer.db.school.entities.xapi.StateEntity

@Dao
interface StateEntityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertAsync(stateEntities: List<StateEntity>)

    /**
     * Retrieve the StateEntity for a singular state retrieval.
     *
     * @param accountPersonUid personUid for the session / active user. This MUST be the personUid
     *        for the actorUid. Used for access control.
     * @param actorUid actor uid
     */
    @Query(
        """
        SELECT StateEntity.*
          FROM StateEntity
         WHERE (SELECT ActorEntity.actorPersonUid
                  FROM ActorEntity
                 WHERE ActorEntity.actorUid = :actorUid) = :accountPersonUid
           AND seActorUid = :actorUid
           AND seHash = :seHash
           AND (   CAST(:includeDeleted AS INTEGER) = 1 
                OR CAST(StateEntity.seDeleted AS INTEGER) = 0)
    """
    )
    suspend fun findByActorAndHash(
        accountPersonUid: Long,
        actorUid: Long,
        seHash: Long,
        includeDeleted: Boolean,
    ): StateEntity?


    @Query(
        """
        SELECT StateEntity.*
          FROM StateEntity
         WHERE (SELECT ActorEntity.actorPersonUid
                  FROM ActorEntity
                 WHERE ActorEntity.actorUid = :actorUid) = :accountPersonUid
           AND seActorUid = :actorUid
           AND seActivityUid = :seActivityUid
           AND (:modifiedSince = 0 OR StateEntity.seLastMod > :modifiedSince)
           AND ((    :registrationUuidHi IS NULL
                 AND StateEntity.seRegistrationHi IS NULL
                 AND :registrationUuidLo IS NULL 
                 AND StateEntity.seRegistrationLo IS NULL)
             OR (    StateEntity.seRegistrationHi = :registrationUuidHi 
                 AND StateEntity.seRegistrationLo = :registrationUuidLo))
           AND StateEntity.seStateId IS NOT NULL  
    """
    )
    suspend fun findByAgentAndActivity(
        accountPersonUid: Long,
        actorUid: Long,
        seActivityUid: Long,
        registrationUuidHi: Long?,
        registrationUuidLo: Long?,
        modifiedSince: Long,
    ): List<StateEntity>

    @Query(
        """
        SELECT StateEntity.seStateId, StateEntity.seLastMod
          FROM StateEntity
         WHERE (SELECT ActorEntity.actorPersonUid
                  FROM ActorEntity
                 WHERE ActorEntity.actorUid = :actorUid) = :accountPersonUid
           AND seActorUid = :actorUid
           AND seActivityUid = :seActivityUid
           AND (:modifiedSince = 0 OR StateEntity.seLastMod > :modifiedSince)
           AND ((    :registrationUuidHi IS NULL
                 AND StateEntity.seRegistrationHi IS NULL
                 AND :registrationUuidLo IS NULL 
                 AND StateEntity.seRegistrationLo IS NULL)
             OR (    StateEntity.seRegistrationHi = :registrationUuidHi 
                 AND StateEntity.seRegistrationLo = :registrationUuidLo))
           AND StateEntity.seStateId IS NOT NULL 
           AND CAST(StateEntity.seDeleted AS INTEGER) = 0      
    """
    )
    suspend fun getStateIds(
        accountPersonUid: Long,
        actorUid: Long,
        seActivityUid: Long,
        registrationUuidHi: Long?,
        registrationUuidLo: Long?,
        modifiedSince: Long,
    ): List<StateIdAndLastModified>


    @Query(
        """
        SELECT StateEntity.*
          FROM StateEntity
         WHERE (SELECT ActorEntity.actorPersonUid
                  FROM ActorEntity
                 WHERE ActorEntity.actorUid = :actorUid) = :accountPersonUid
           AND seActorUid = :actorUid
           AND seActivityUid = :seActivityUid 
           AND ((    :registrationUuidHi IS NULL
                 AND StateEntity.seRegistrationHi IS NULL
                 AND :registrationUuidLo IS NULL 
                 AND StateEntity.seRegistrationLo IS NULL)
             OR (    StateEntity.seRegistrationHi = :registrationUuidHi 
                 AND StateEntity.seRegistrationLo = :registrationUuidLo))
           AND StateEntity.seH5PSubContentId IS NOT NULL      
           AND CAST(StateEntity.seH5PPreloaded AS INTEGER) = 1      
    """
    )
    suspend fun getH5PPreload(
        accountPersonUid: Long,
        actorUid: Long,
        seActivityUid: Long,
        registrationUuidHi: Long?,
        registrationUuidLo: Long?,
    ): List<StateEntity>
}