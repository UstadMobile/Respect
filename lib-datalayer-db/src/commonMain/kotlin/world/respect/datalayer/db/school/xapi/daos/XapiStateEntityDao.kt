package world.respect.datalayer.db.school.xapi.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import world.respect.datalayer.db.school.composite.StateIdAndLastModified
import world.respect.datalayer.db.school.xapi.entities.XapiStateEntity

@Dao
interface XapiStateEntityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertAsync(stateEntities: List<XapiStateEntity>)

    /**
     * Retrieve the StateEntity for a singular state retrieval.
     *
     * @param accountPersonUid personUid for the session / active user. This MUST be the personUid
     *        for the actorUid. Used for access control.
     * @param actorUid actor uid
     */
    @Query(
        """
        SELECT XapiStateEntity.*
          FROM XapiStateEntity
         WHERE (SELECT XapiActorEntity.actorPersonUid
                  FROM XapiActorEntity
                 WHERE XapiActorEntity.actorUid = :actorUid) = :accountPersonUid
           AND seActorUid = :actorUid
           AND seHash = :seHash
           AND (   CAST(:includeDeleted AS INTEGER) = 1 
                OR CAST(XapiStateEntity.seDeleted AS INTEGER) = 0)
    """
    )
    suspend fun findByActorAndHash(
        accountPersonUid: Long,
        actorUid: Long,
        seHash: Long,
        includeDeleted: Boolean,
    ): XapiStateEntity?


    @Query(
        """
        SELECT XapiStateEntity.*
          FROM XapiStateEntity
         WHERE (SELECT XapiActorEntity.actorPersonUid
                  FROM XapiActorEntity
                 WHERE XapiActorEntity.actorUid = :actorUid) = :accountPersonUid
           AND seActorUid = :actorUid
           AND seActivityUid = :seActivityUid
           AND (:modifiedSince = 0 OR XapiStateEntity.seLastMod > :modifiedSince)
           AND ((    :registrationUuidHi IS NULL
                 AND XapiStateEntity.seRegistrationHi IS NULL
                 AND :registrationUuidLo IS NULL 
                 AND XapiStateEntity.seRegistrationLo IS NULL)
             OR (    XapiStateEntity.seRegistrationHi = :registrationUuidHi 
                 AND XapiStateEntity.seRegistrationLo = :registrationUuidLo))
           AND XapiStateEntity.seStateId IS NOT NULL  
    """
    )
    suspend fun findByAgentAndActivity(
        accountPersonUid: Long,
        actorUid: Long,
        seActivityUid: Long,
        registrationUuidHi: Long?,
        registrationUuidLo: Long?,
        modifiedSince: Long,
    ): List<XapiStateEntity>

    @Query(
        """
        SELECT XapiStateEntity.seStateId, XapiStateEntity.seLastMod
          FROM XapiStateEntity
         WHERE (SELECT XapiActorEntity.actorPersonUid
                  FROM XapiActorEntity
                 WHERE XapiActorEntity.actorUid = :actorUid) = :accountPersonUid
           AND seActorUid = :actorUid
           AND seActivityUid = :seActivityUid
           AND (:modifiedSince = 0 OR XapiStateEntity.seLastMod > :modifiedSince)
           AND ((    :registrationUuidHi IS NULL
                 AND XapiStateEntity.seRegistrationHi IS NULL
                 AND :registrationUuidLo IS NULL 
                 AND XapiStateEntity.seRegistrationLo IS NULL)
             OR (    XapiStateEntity.seRegistrationHi = :registrationUuidHi 
                 AND XapiStateEntity.seRegistrationLo = :registrationUuidLo))
           AND XapiStateEntity.seStateId IS NOT NULL 
           AND CAST(XapiStateEntity.seDeleted AS INTEGER) = 0      
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
        SELECT XapiStateEntity.*
          FROM XapiStateEntity
         WHERE (SELECT XapiActorEntity.actorPersonUid
                  FROM XapiActorEntity
                 WHERE XapiActorEntity.actorUid = :actorUid) = :accountPersonUid
           AND seActorUid = :actorUid
           AND seActivityUid = :seActivityUid 
           AND ((    :registrationUuidHi IS NULL
                 AND XapiStateEntity.seRegistrationHi IS NULL
                 AND :registrationUuidLo IS NULL 
                 AND XapiStateEntity.seRegistrationLo IS NULL)
             OR (    XapiStateEntity.seRegistrationHi = :registrationUuidHi 
                 AND XapiStateEntity.seRegistrationLo = :registrationUuidLo))
           AND XapiStateEntity.seH5PSubContentId IS NOT NULL      
           AND CAST(XapiStateEntity.seH5PPreloaded AS INTEGER) = 1      
    """
    )
    suspend fun getH5PPreload(
        accountPersonUid: Long,
        actorUid: Long,
        seActivityUid: Long,
        registrationUuidHi: Long?,
        registrationUuidLo: Long?,
    ): List<XapiStateEntity>
}