package world.respect.datalayer.db.opds.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.db.opds.entities.PersonPasskeyEntity

@Dao
abstract class PersonPasskeyEntityDao {

    @Insert
    abstract suspend fun insertAsync(personPasskey: PersonPasskeyEntity): Long

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
         WHERE ppId = :id
    """)
    abstract suspend fun findPersonPasskeyFromClientDataJson(id: String): PersonPasskeyEntity?

    @Query("""
        UPDATE PersonPasskeyEntity
           SET isRevoked = ${PersonPasskeyEntity.REVOKED}
         WHERE personPasskeyUid = :uid
    """)
    abstract suspend fun revokePersonPasskey(uid: Long)
}
