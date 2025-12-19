package world.respect.datalayer.db.school.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.db.school.entities.PersonBadgeEntity

@Dao
interface PersonQrBadgeEntityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(personQrCodeEntity: PersonBadgeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAsyncList(list: List<PersonBadgeEntity>)

    @Query("SELECT * FROM PersonBadgeEntity")
    suspend fun getAll(): List<PersonBadgeEntity>

    @Query("""
        SELECT PersonBadgeEntity.*
          FROM PersonBadgeEntity
         WHERE PersonBadgeEntity.pqrGuidNum = :personGuidNum
    """)
    suspend fun findAll(
        personGuidNum: Long
    ): List<PersonBadgeEntity>

    @Query(
        """
           SELECT PersonBadgeEntity.* 
             FROM PersonBadgeEntity
            WHERE pqrGuid = :guid
        """
    )
    suspend fun findByGuid(guid: String): PersonBadgeEntity?

    @Query(
        """
        SELECT PersonBadgeEntity.*
          FROM PersonBadgeEntity
         WHERE PersonBadgeEntity.pqrQrCodeUrl = :qrCodeUrl
    """
    )
    suspend fun findByQrCodeUrl(qrCodeUrl: String): PersonBadgeEntity?

    @Query(
        """
        SELECT PersonBadgeEntity.pqrLastModified
          FROM PersonBadgeEntity
         WHERE PersonBadgeEntity.pqrGuidNum = :uidNum
    """
    )
    suspend fun getLastModifiedByUidNum(
        uidNum: Long
    ): Long?

    @Query(
        """
        SELECT PersonBadgeEntity.*
          FROM PersonBadgeEntity
         WHERE PersonBadgeEntity.pqrGuidNum IN (:uids)
    """
    )
    suspend fun findByUidList(
        uids: List<Long>
    ): List<PersonBadgeEntity>

    @Query(
        """
        SELECT PersonBadgeEntity.*
          FROM PersonBadgeEntity
         WHERE PersonBadgeEntity.pqrGuid = :personGuid
    """
    )
    suspend fun findAllByPersonGuid(
        personGuid: String
    ): List<PersonBadgeEntity>

    @Query(
        """
        SELECT PersonBadgeEntity.*
          FROM PersonBadgeEntity
         WHERE PersonBadgeEntity.pqrGuidNum = :personGuid
    """
    )
    fun findAllByPersonGuidAsFlow(
        personGuid: Long
    ): Flow<List<PersonBadgeEntity>>

    @Query(
        """
        SELECT EXISTS(
            SELECT 1 
            FROM PersonBadgeEntity 
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
        DELETE FROM PersonBadgeEntity 
         WHERE pqrGuid = :guidNum
    """
    )
    suspend fun deleteByGuid(guidNum: Long)

    @Query("""
        SELECT * 
         FROM PersonBadgeEntity
        WHERE pqrGuidNum = :guidnum
    """)
    fun findByGuidHashAsFlow(guidnum: Long): Flow<PersonBadgeEntity?>


}