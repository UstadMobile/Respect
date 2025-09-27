package world.respect.datalayer.db.opds.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.db.opds.entities.PersonPasskeyEntity

@Dao
abstract class PersonPasskeyEntityDao {

    @Insert
    abstract suspend fun insertAsync(
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
         WHERE ppId = :id
    """)
    abstract suspend fun findPersonPasskeyFromClientDataJson(id: String): PersonPasskeyEntity?

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
    """)
    abstract suspend fun findAll(personGuidNumber: Long): List<PersonPasskeyEntity>

    @Query("""
        SELECT PersonPasskeyEntity.*
          FROM PersonPasskeyEntity
         WHERE PersonPasskeyEntity.ppPersonUid = :personGuidNumber
    """)
    abstract fun findAllAsFlow(personGuidNumber: Long): Flow<List<PersonPasskeyEntity>>


}
