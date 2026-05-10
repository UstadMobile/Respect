package world.respect.datalayer.db.school.daos

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.db.school.entities.ChangeHistoryChangeEntity
import world.respect.datalayer.db.school.entities.ChangeHistoryEntity
import world.respect.datalayer.db.school.entities.ChangeHistoryWithChanges
import world.respect.datalayer.school.model.ChangeHistoryTableEnum

@Dao
interface ChangeHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(
        entity: ChangeHistoryEntity
    )


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChanges(
        entities: List<ChangeHistoryChangeEntity>
    )

    @Transaction
    suspend fun insertHistoryWithChanges(
        history: ChangeHistoryEntity,
        changes: List<ChangeHistoryChangeEntity>
    ) {
        insertHistory(history)
        insertChanges(changes)
    }

    @Query("""
         SELECT *
           FROM ChangeHistoryEntity
          WHERE (:table IS NULL OR hTable = :table)
            AND (:whoGuidHash = 0 OR hWhoGuidHash = :whoGuidHash)
           ORDER BY hTimestamp DESC
     """)
    fun listAsPagingSource(
        table: ChangeHistoryTableEnum?,
        whoGuidHash: Long,
    ): PagingSource<Int, ChangeHistoryWithChanges>

    @Query(
        """
        SELECT *
          FROM ChangeHistoryEntity
         WHERE hTableGuid = :tableGuid
     """
    )
    suspend fun findByGuid(tableGuid: String): List<ChangeHistoryWithChanges>?


    @Query("""
        SELECT *
          FROM ChangeHistoryEntity
         WHERE hGuid = :guid
     """)
    fun findByGuidAsFlow(guid: String): Flow<ChangeHistoryWithChanges?>


    @Query("""
        SELECT *
          FROM ChangeHistoryEntity
         ORDER BY hGuidHash DESC
    """)
    fun listAsPagingSource(): PagingSource<Int, ChangeHistoryWithChanges>

}