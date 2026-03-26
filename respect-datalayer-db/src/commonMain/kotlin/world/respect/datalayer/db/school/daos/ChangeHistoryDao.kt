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
        SELECT ChangeHistoryEntity.hLastModified
          FROM ChangeHistoryEntity
         WHERE ChangeHistoryEntity.hGuidHash = :uidNum
    """)
    suspend fun getLastModifiedByUidNum(uidNum: Long): Long?

    @Query("""
         SELECT * 
           FROM ChangeHistoryEntity
          WHERE hTable = :tableEnum
           AND hTableGuid IN (:uids)
    """)
    suspend fun getByTableAndUids(
        tableEnum: ChangeHistoryTableEnum,
        uids: List<String>
    ): List<ChangeHistoryWithChanges>

    @Query("""
         SELECT *
           FROM ChangeHistoryEntity
          WHERE hTable = :tableEnum
           AND hTableGuid IN (:uids)
           AND hGuidHash IN (
              SELECT hcHistoryGuidHash
                FROM ChangeHistoryChangeEntity
               WHERE hcSynced = 0
             )
      """)
    suspend fun getParentsWithUnsyncedChanges(
        tableEnum: ChangeHistoryTableEnum,
        uids: List<String>
    ): List<ChangeHistoryEntity>
    @Query("""
         SELECT *
           FROM ChangeHistoryEntity
          WHERE (:table IS NULL OR hTable = :table)
            AND (:whoGuidHash = 0 OR hWhoGuidHash = :whoGuidHash)
           ORDER BY hLastModified DESC
     """)
    fun listAsPagingSource(
        table: ChangeHistoryTableEnum?,
        whoGuidHash: Long,
    ): PagingSource<Int, ChangeHistoryWithChanges>
    @Query("""
        SELECT *
          FROM ChangeHistoryChangeEntity
         WHERE hcHistoryGuidHash IN (:historyIds)
           AND hcSynced = 0
    """)
    suspend fun getUnsyncedChanges(
        historyIds: List<Long>
    ): List<ChangeHistoryChangeEntity>

    @Transaction
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
         WHERE hTableGuid = :guid
         ORDER BY hLastModified DESC
     """)
    fun findByGuidAsFlow(guid: String): Flow<List<ChangeHistoryWithChanges>>


    @Query("""
         UPDATE ChangeHistoryChangeEntity
           SET hcSynced = 1
             WHERE hcHistoryGuidHash IN (:historyGuids)
      """)
    suspend fun markByHistoryGuids(historyGuids: List<Long>)

    @Query("""
        SELECT *
          FROM ChangeHistoryEntity
         ORDER BY hGuidHash DESC
    """)
    fun listAsPagingSource(): PagingSource<Int, ChangeHistoryWithChanges>

}