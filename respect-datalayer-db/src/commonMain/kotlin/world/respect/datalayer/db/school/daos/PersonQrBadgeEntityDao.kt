package world.respect.datalayer.db.school.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.db.school.entities.PersonQrBadgeEntity

@Dao
interface PersonQrBadgeEntityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(personQrCodeEntity: PersonQrBadgeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAsyncList(list: List<PersonQrBadgeEntity>)

    @Query("SELECT * FROM PersonQrBadgeEntity")
    suspend fun getAll(): List<PersonQrBadgeEntity>

    @Query(
        """
        SELECT PersonQrBadgeEntity.*
          FROM PersonQrBadgeEntity
         WHERE PersonQrBadgeEntity.pqrGuidNum = :personGuidNum
         AND (:includeDeleted OR PersonQrBadgeEntity.pqrStatus = 1) 
    """
    )
    suspend fun findAll(
        personGuidNum: Long,
        includeDeleted: Boolean = false
    ): List<PersonQrBadgeEntity>

    @Query(
        """
           SELECT PersonQrBadgeEntity.* 
             FROM PersonQrBadgeEntity
            WHERE pqrGuid = :guid
        """
    )
    suspend fun findByGuid(guid: String): PersonQrBadgeEntity?

    @Query(
        """
        SELECT PersonQrBadgeEntity.*
          FROM PersonQrBadgeEntity
         WHERE PersonQrBadgeEntity.pqrQrCodeUrl = :qrCodeUrl
    """
    )
    suspend fun findByQrCodeUrl(qrCodeUrl: String): PersonQrBadgeEntity?

    @Query(
        """
        SELECT PersonQrBadgeEntity.pqrLastModified
          FROM PersonQrBadgeEntity
         WHERE PersonQrBadgeEntity.pqrGuidNum = :uidNum
    """
    )
    suspend fun getLastModifiedByUidNum(
        uidNum: Long
    ): Long?

    @Query(
        """
        SELECT PersonQrBadgeEntity.*
          FROM PersonQrBadgeEntity
         WHERE PersonQrBadgeEntity.pqrGuidNum IN (:uids)
    """
    )
    suspend fun findByUidList(
        uids: List<Long>
    ): List<PersonQrBadgeEntity>

    @Query(
        """
        SELECT PersonQrBadgeEntity.*
          FROM PersonQrBadgeEntity
         WHERE PersonQrBadgeEntity.pqrGuid = :personGuid
    """
    )
    suspend fun findAllByPersonGuid(
        personGuid: String
    ): List<PersonQrBadgeEntity>

    @Query(
        """
        SELECT PersonQrBadgeEntity.*
          FROM PersonQrBadgeEntity
         WHERE PersonQrBadgeEntity.pqrGuidNum = :personGuid
         AND (:includeDeleted OR PersonQrBadgeEntity.pqrStatus = 1)  

    """
    )
    fun findAllByPersonGuidAsFlow(
        personGuid: Long,
        includeDeleted: Boolean = false
    ): Flow<List<PersonQrBadgeEntity>>

    @Query(
        """
        SELECT EXISTS(
            SELECT 1 
            FROM PersonQrBadgeEntity 
            WHERE pqrQrCodeUrl = :qrCodeUrl
              AND pqrGuid != :excludePersonGuid
        )
    """
    )
    suspend fun existsByQrCodeUrlExcludingPerson(
        qrCodeUrl: String,
        excludePersonGuid: Long
    ): Boolean

    @Query(
        """
        DELETE FROM PersonQrBadgeEntity 
         WHERE pqrGuid = :guidNum
    """
    )
    suspend fun deleteByGuid(guidNum: Long)

    @Query(
        """
        SELECT * 
         FROM PersonQrBadgeEntity
        WHERE pqrGuidNum = :guidnum
    """
    )
    fun findByGuidHashAsFlow(guidnum: Long): Flow<PersonQrBadgeEntity?>


}