package world.respect.datalayer.db.school.daos

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.db.school.adapters.AssignmentEntities
import world.respect.datalayer.db.school.daos.PersonEntityDao.Companion.SELECT_AUTHENTICATED_PERMISSION_PERSON_UIDS_SQL
import world.respect.datalayer.db.school.entities.AssignmentEntity
import world.respect.datalayer.school.model.PermissionFlags
import world.respect.datalayer.school.model.StatusEnum

@Dao
interface AssignmentEntityDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entities: List<AssignmentEntity>)

    @Query("""
        SELECT AssignmentEntity.*
          FROM AssignmentEntity
         WHERE (:uidNum = 0 OR AssignmentEntity.aeUidNum = :uidNum)
    """)
    suspend fun list(
        uidNum: Long
    ): List<AssignmentEntities>

    @Query("""
          WITH ${PersonEntityDao.AUTHENTICATED_PERMISSION_PERSON_UIDS_CTE_SQL},
               ${ClassEntityDao.AUTHENTICATED_USER_ENROLLMENTS_CTE_SQL}
              
        SELECT AssignmentEntity.*
          FROM AssignmentEntity
               JOIN ClassEntity 
                    ON ClassEntity.cGuidHash = AssignmentEntity.aeClassUidNum
         WHERE (:uidNum = 0 OR AssignmentEntity.aeUidNum = :uidNum)
           AND (${ClassEntityDao.CLASS_PERMISSION_CHECK_SQL})
    """)
    fun listAsPagingSource(
        authenticatedPersonUidNum: Long,
        uidNum: Long,
        requiredPermission: Long = PermissionFlags.CLASS_READ,
    ): PagingSource<Int, AssignmentEntities>


    @Query("""
        SELECT AssignmentEntity.aeLastModified
          FROM AssignmentEntity
         WHERE AssignmentEntity.aeUidNum = :uidNum
    """)
    suspend fun getLastModifiedByUidNum(uidNum: Long): Long?

    @Query("""
        SELECT AssignmentEntity.*
          FROM AssignmentEntity
         WHERE AssignmentEntity.aeUidNum = :uidNum
    """)
    suspend fun findByUidNum(uidNum: Long): AssignmentEntities?

    @Query("""
        SELECT AssignmentEntity.*
          FROM AssignmentEntity
         WHERE AssignmentEntity.aeUidNum = :uidNum
    """)
    fun findByUidNumAsFlow(uidNum: Long): Flow<AssignmentEntities?>

    @Query("""
        SELECT AssignmentEntity.*
          FROM AssignmentEntity
         WHERE AssignmentEntity.aeUidNum IN (:uidNums)
    """)
    suspend fun findByUidNums(uidNums: List<Long>): List<AssignmentEntities>

    companion object {



    }

}