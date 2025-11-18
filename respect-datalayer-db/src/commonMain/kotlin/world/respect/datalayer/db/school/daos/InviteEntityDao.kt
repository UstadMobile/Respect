package world.respect.datalayer.db.school.daos

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.db.school.entities.InviteEntity

@Dao
interface InviteEntityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(inviteEntity: InviteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(invites: List<InviteEntity>)

    @Query("""
        SELECT InviteEntity.iLastModified
          FROM InviteEntity
         WHERE InviteEntity.iGuidHash = :guidHash
         LIMIT 1
    """)
    suspend fun getLastModifiedByGuid(guidHash: Long): Long?

    @Query("""
        SELECT * 
          FROM InviteEntity
         WHERE iGuidHash = :guidHash
         LIMIT 1
    """)
    suspend fun findByGuidHash(guidHash: Long): InviteEntity?

    @Query("""
        SELECT * 
          FROM InviteEntity
         WHERE iGuidHash = :guidHash
         LIMIT 1
    """)
    fun findByGuidHashAsFlow(guidHash: Long): Flow<InviteEntity?>

    @Query("""
        SELECT * 
          FROM InviteEntity
         WHERE iGuidHash IN (:uidNums)
    """)
    suspend fun findByUidList(uidNums: List<Long>): List<InviteEntity>

    @Query("""
        DELETE FROM InviteEntity
         WHERE iGuidHash = :guidHash
    """)
    suspend fun deleteByGuidHash(guidHash: Long)
}
