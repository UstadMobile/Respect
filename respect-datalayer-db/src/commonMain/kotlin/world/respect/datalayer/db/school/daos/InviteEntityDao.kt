package world.respect.datalayer.db.school.daos

import androidx.paging.PagingSource
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.db.school.entities.InviteEntity
import world.respect.libutil.util.time.systemTimeInMillis

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
        UPDATE InviteEntity
        SET iInviteStatus = :status,
            iLastModified = :lastModified
        WHERE iGuid = :guid
    """)
    suspend fun updateInviteStatus(
        guid: String,
        status: Int = InviteEntity.STATUS_ACCEPTED,
        lastModified: Long = systemTimeInMillis()
    )
    @Query("""
        SELECT * 
          FROM InviteEntity
         WHERE iCode = :code
         LIMIT 1
    """)
    suspend fun getInviteByInviteCode(code:String): InviteEntity?
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
         WHERE iGuid = :guid
         LIMIT 1
    """)
    suspend fun findByGuid(guid: String): InviteEntity?

    @Query("""
        SELECT * 
          FROM InviteEntity
         WHERE iGuidHash = :guidHash
         LIMIT 1
    """)
    fun findByGuidHashAsFlow(guidHash: Long): Flow<InviteEntity?>

    @Query("""  
        SELECT InviteEntity.* 
         FROM InviteEntity
        WHERE (:guidHash = 0 OR InviteEntity.iGuidHash = :guidHash)
          AND (:code IS NULL 
                OR InviteEntity.iCode = :code)
          """)
    fun findAllAsPagingSource(
        guidHash: Long = 0,
        code: String? = null,
    ): PagingSource<Int, InviteEntity>

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
