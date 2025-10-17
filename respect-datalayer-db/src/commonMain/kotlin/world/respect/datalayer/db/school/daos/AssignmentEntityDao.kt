package world.respect.datalayer.db.school.daos

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import world.respect.datalayer.db.school.adapters.AssignmentEntities
import world.respect.datalayer.db.school.entities.AssignmentEntity

@Dao
interface AssignmentEntityDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entities: List<AssignmentEntity>)

    @Query("""
        SELECT AssignmentEntity.*
          FROM AssignmentEntity
    """)
    suspend fun list(): List<AssignmentEntities>

    @Query("""
        SELECT AssignmentEntity.*
          FROM AssignmentEntity
    """)
    fun listAsPagingSource(): PagingSource<Int, AssignmentEntities>


    @Query("""
        SELECT AssignmentEntity.aeLastModified
          FROM AssignmentEntity
         WHERE AssignmentEntity.aeUidNum = :uidNum
    """)
    suspend fun getLastModifiedByUidNum(uidNum: Long): Long?

    @Query("""
        SELECT AssignmentEntity.*
          FROM AssignmentEntity
         WHERE AssignmentEntity.aeUidNum IN (:uidNums)
    """)
    suspend fun findByUidNums(uidNums: List<Long>): List<AssignmentEntities>

}