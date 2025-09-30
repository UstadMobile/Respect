package world.respect.datalayer.db.school.daos

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.db.school.entities.ReportEntity

@Dao
interface ReportEntityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun putReport(reportEntity: ReportEntity)

    @Transaction
    @Query(
        """
        SELECT ReportEntity.*
          FROM ReportEntity
         WHERE ReportEntity.rGuidHash IN (:uidNums) 
    """
    )
    suspend fun findByUidList(uidNums: List<Long>): List<ReportEntity>

    @Query(
        """
        SELECT ReportEntity.rLastModified
          FROM ReportEntity
         WHERE ReportEntity.rGuidHash = :guidHash
         LIMIT 1
    """
    )
    suspend fun getLastModifiedByGuid(guidHash: Long): Long?

    @Transaction
    @Query(
        """
        SELECT * 
        FROM ReportEntity
        WHERE rIsTemplate = :template
    """
    )
    fun getAllReportsByTemplate(template: Boolean): Flow<List<ReportEntity>>

    @Query(
        """
        SELECT * 
         FROM ReportEntity
        WHERE rGuidHash = :guidHash
    """
    )
    suspend fun findByGuidHash(guidHash: Long): ReportEntity?

    @Transaction
    @Query(
        """
        SELECT * 
         FROM ReportEntity
        WHERE rGuidHash = :guidHash
    """
    )
    fun findByGuidHashAsFlow(guidHash: Long): Flow<ReportEntity?>

    @Transaction
    @Query("DELETE FROM ReportEntity WHERE rGuidHash = :guidHash")
    suspend fun deleteReportByGuidHash(guidHash: Long)

    @Transaction
    @Query(
        """
        SELECT ReportEntity.* 
         FROM ReportEntity
        WHERE ReportEntity.rStored > :since 
          AND (:guidHash = 0 OR ReportEntity.rGuidHash = :guidHash)
     ORDER BY ReportEntity.rTitle
    """
    )
    fun findAllAsPagingSource(
        since: Long = 0,
        guidHash: Long = 0,
    ): PagingSource<Int, ReportEntity>
}