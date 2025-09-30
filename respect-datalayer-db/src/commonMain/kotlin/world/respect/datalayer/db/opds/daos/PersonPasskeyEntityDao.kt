package world.respect.datalayer.db.opds.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.db.school.entities.PersonPasskeyEntity

@Dao
abstract class PersonPasskeyEntityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertAsync(
        personPasskey: List<PersonPasskeyEntity>
    )

    @Query("""
        SELECT ppId
          FROM PersonPasskeyEntity
    """)
    abstract suspend fun allPasskey(): List<String>

    @Query("""
        SELECT * 
          FROM PersonPasskeyEntity
         WHERE isRevoked = ${PersonPasskeyEntity.NOT_REVOKED}
           AND ppPersonUid = :uid
    """)
    abstract fun getAllActivePasskeys(uid: Long): Flow<List<PersonPasskeyEntity>>
    @Query("""
        SELECT * 
          FROM PersonPasskeyEntity
         WHERE isRevoked = ${PersonPasskeyEntity.NOT_REVOKED}
           AND ppPersonUid = :uid
    """)
    abstract suspend fun getAllActivePasskeysList(uid: Long): List<PersonPasskeyEntity>

    @Query("""
        SELECT *
          FROM PersonPasskeyEntity
         WHERE PersonPasskeyEntity.ppId = :id
           AND PersonPasskeyEntity.isRevoked = ${PersonPasskeyEntity.NOT_REVOKED}
    """)
    abstract suspend fun findPersonPasskeyFromClientDataJson(
        id: String,
    ): PersonPasskeyEntity?

    @Query("""
        SELECT PersonPasskeyEntity.*
          FROM PersonPasskeyEntity
         WHERE PersonPasskeyEntity.personPasskeyUid = :uid
           AND PersonPasskeyEntity.isRevoked = ${PersonPasskeyEntity.NOT_REVOKED}
    """)
    abstract suspend fun findByPersonPasskeyUid(
        uid: Long,
    ): PersonPasskeyEntity?


    @Query("""
        UPDATE PersonPasskeyEntity
           SET isRevoked = ${PersonPasskeyEntity.REVOKED}
         WHERE ppPersonUid = :personUidNum
           AND ppId = :passKeyId
    """)
    abstract suspend fun revokePersonPasskey(
        personUidNum: Long,
        passKeyId: String,
    )

    @Query("""
        SELECT PersonPasskeyEntity.*
          FROM PersonPasskeyEntity
         WHERE PersonPasskeyEntity.ppPersonUid = :personGuidNumber
           AND (:includeRevoked = 1 
                OR PersonPasskeyEntity.isRevoked = ${PersonPasskeyEntity.NOT_REVOKED})
    """)
    abstract suspend fun findAll(
        personGuidNumber: Long,
        includeRevoked: Int,
    ): List<PersonPasskeyEntity>

    @Query("""
        SELECT PersonPasskeyEntity.*
          FROM PersonPasskeyEntity
         WHERE PersonPasskeyEntity.ppPersonUid = :personGuidNumber
           AND (:includeRevoked = 1 
                OR PersonPasskeyEntity.isRevoked = ${PersonPasskeyEntity.NOT_REVOKED})
    """)
    abstract fun findAllAsFlow(
        personGuidNumber: Long,
        includeRevoked: Int,
    ): Flow<List<PersonPasskeyEntity>>

    @Query("""
        SELECT PersonPasskeyEntity.ppLastModified
          FROM PersonPasskeyEntity
         WHERE PersonPasskeyEntity.ppPersonUid = :personUidNum
           AND PersonPasskeyEntity.ppId = :passKeyId
    """)
    abstract suspend fun getLastModifiedByPersonUidAndKeyId(
        personUidNum: Long,
        passKeyId: String,
    ): Long?


}
