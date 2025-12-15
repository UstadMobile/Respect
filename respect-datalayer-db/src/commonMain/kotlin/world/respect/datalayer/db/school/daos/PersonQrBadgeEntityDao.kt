package world.respect.datalayer.db.school.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.db.school.entities.PersonQrCodeEntity

@Dao
interface PersonQrBadgeEntityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(personQrCodeEntity: PersonQrCodeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAsyncList(list: List<PersonQrCodeEntity>)

    @Query(
        """
           SELECT PersonQrCodeEntity.* 
             FROM PersonQrCodeEntity
            WHERE pqrGuid = :guid
        """
    )
    suspend fun findByGuid(guid: String): PersonQrCodeEntity?

    @Query(
        """
        SELECT PersonQrCodeEntity.*
          FROM PersonQrCodeEntity
         WHERE PersonQrCodeEntity.pqrQrCodeUrl = :qrCodeUrl
    """
    )
    suspend fun findByQrCodeUrl(qrCodeUrl: String): PersonQrCodeEntity?

    @Query(
        """
        SELECT PersonQrCodeEntity.pqrLastModified
          FROM PersonQrCodeEntity
         WHERE PersonQrCodeEntity.pqrGuidNum = :uidNum
    """
    )
    suspend fun getLastModifiedByUidNum(
        uidNum: Long
    ): Long?

    @Query(
        """
        SELECT PersonQrCodeEntity.*
          FROM PersonQrCodeEntity
         WHERE PersonQrCodeEntity.pqrGuidNum IN (:uids)
    """
    )
    suspend fun findByUidList(
        uids: List<Long>
    ): List<PersonQrCodeEntity>

    @Query(
        """
        SELECT PersonQrCodeEntity.*
          FROM PersonQrCodeEntity
         WHERE PersonQrCodeEntity.pqrGuid = :personGuid
    """
    )
    suspend fun findAllByPersonGuid(
        personGuid: String
    ): List<PersonQrCodeEntity>

    @Query(
        """
        SELECT PersonQrCodeEntity.*
          FROM PersonQrCodeEntity
         WHERE PersonQrCodeEntity.pqrGuidNum = :personGuid
    """
    )
    fun findAllByPersonGuidAsFlow(
        personGuid: Long
    ): Flow<List<PersonQrCodeEntity>>

    @Query(
        """
        DELETE FROM PersonQrCodeEntity 
         WHERE pqrGuid = :guid
    """
    )
    suspend fun deleteByGuid(guid: String)


}